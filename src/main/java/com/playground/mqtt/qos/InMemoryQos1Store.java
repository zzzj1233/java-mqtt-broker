package com.playground.mqtt.qos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * In-memory QoS1 state store.
 *
 * <p>State tables are split by direction:
 * - inboundStates: publisher -> broker (duplicate detection / ack lifecycle)
 * - outboundStates: broker -> subscriber (inflight / retry / ack lifecycle)
 */
public final class InMemoryQos1Store {

    private final ConcurrentMap<String, Map<Integer, Qos1PacketState>> inboundStates =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Map<Integer, Qos1InflightRecord>> outboundStates =
            new ConcurrentHashMap<>();

    public boolean updateInboundState(String clientId, int packetId, Qos1PacketState state) {
        validate(clientId, packetId);
        return updateState(inboundStates, clientId, packetId, state);
    }

    public boolean updateOutboundState(String clientId, int packetId, Qos1PacketState state) {
        validate(clientId, packetId);
        Map<Integer, Qos1InflightRecord> statesMap = outboundStates.computeIfAbsent(
                clientId,
                ignored -> new ConcurrentHashMap<>()
        );
        long nowNanos = System.nanoTime();

        Qos1InflightRecord updated = statesMap.compute(
                packetId,
                (ignored, previousRecord) -> {
                    Qos1PacketState previousState = previousRecord == null ? null : previousRecord.state();
                    if (!state.canTransitionFrom(previousState)) {
                        return previousRecord;
                    }
                    if (state == Qos1PacketState.IN_FLIGHT) {
                        throw new IllegalStateException(
                                "Outbound IN_FLIGHT must be created via createOutboundInflight, clientId="
                                        + clientId + ", packetId=" + packetId
                        );
                    }
                    if (state == Qos1PacketState.ACKED && previousRecord != null) {
                        return previousRecord.toAcked(nowNanos);
                    }
                    return previousRecord;
                }
        );
        return updated != null && updated.state() == state;
    }

    /**
     * Creates outbound inflight record atomically (null -> IN_FLIGHT).
     *
     * @return true if record is created for the first time; false if existing record already present.
     */
    public boolean createOutboundInflight(
            String clientId,
            int packetId,
            RefCountedPublishPayloadId publishPayloadId
    ) {
        validate(clientId, packetId);
        if (publishPayloadId == null) {
            throw new IllegalArgumentException("publishPayloadId must not be null");
        }
        Map<Integer, Qos1InflightRecord> states = outboundStates.computeIfAbsent(
                clientId,
                ignored -> new ConcurrentHashMap<>()
        );
        long nowNanos = System.nanoTime();
        AtomicBoolean created = new AtomicBoolean(false);
        states.compute(
                packetId,
                (ignored, previousRecord) -> {
                    if (previousRecord == null) {
                        created.set(true);
                        return Qos1InflightRecord.firstSend(nowNanos, publishPayloadId);
                    }
                    return previousRecord;
                }
        );
        return created.get();
    }


    public boolean cleanStatus(String clientId, int packetId, Qos1PacketState state) {
        return removeOutboundIfState(clientId, packetId, state) != null;
    }

    public boolean removeInboundIfState(String clientId, int packetId, Qos1PacketState state) {

        validate(clientId, packetId);

        Map<Integer, Qos1PacketState> states = inboundStates.get(clientId);

        if (states == null) {
            return false;
        }

        boolean removed = states.remove(packetId, state);

        if (states.isEmpty()) {
            inboundStates.remove(clientId, states);
        }

        return removed;
    }


    public Qos1InflightRecord removeOutboundIfState(String clientId, int packetId, Qos1PacketState state) {
        validate(clientId, packetId);
        Map<Integer, Qos1InflightRecord> states = outboundStates.get(clientId);
        if (states == null) {
            return null;
        }
        AtomicReference<Qos1InflightRecord> removed = new AtomicReference<>(null);
        states.compute(
                packetId,
                (ignored, prevRecord) -> {
                    if (prevRecord != null && prevRecord.state() == state) {
                        removed.set(prevRecord);
                        return null;
                    }
                    return prevRecord;
                }
        );
        if (states.isEmpty()) {
            outboundStates.remove(clientId, states);
        }
        return removed.get();
    }


    /**
     * Compatibility API for current PublishHandler inbound dedup logic.
     * First-seen means transition null -> IN_FLIGHT.
     */
    public boolean markFirstSeen(String clientId, int packetId) {
        return updateInboundState(clientId, packetId, Qos1PacketState.IN_FLIGHT);
    }

    /**
     * Compatibility API for inbound ack lifecycle.
     * Transition IN_FLIGHT -> ACKED.
     */
    public boolean markAcked(String clientId, int packetId) {
        return updateInboundState(clientId, packetId, Qos1PacketState.ACKED);
    }

    /**
     * Clears all tracked packet states for this client in both directions.
     */
    public void clearClient(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return;
        }
        inboundStates.remove(clientId);
        outboundStates.remove(clientId);
    }

    private static boolean updateState(
            ConcurrentMap<String, Map<Integer, Qos1PacketState>> statesByClient,
            String clientId,
            int packetId,
            Qos1PacketState state
    ) {
        return statesByClient.computeIfAbsent(clientId, ignored -> new ConcurrentHashMap<>())
                .compute(packetId, (ignored, prevState) -> state.canTransitionFrom(prevState) ? state : prevState)
                == state;
    }

    private static void validate(String clientId, int packetId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId must not be blank");
        }
        if (packetId <= 0 || packetId > 0xFFFF) {
            throw new IllegalArgumentException("packetId must be in range 1..65535");
        }
    }

    public ConcurrentMap<String, Map<Integer, Qos1PacketState>> getInboundStates() {
        return inboundStates;
    }

    public ConcurrentMap<String, Map<Integer, Qos1InflightRecord>> getOutboundStates() {
        return outboundStates;
    }
}
