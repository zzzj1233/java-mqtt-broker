package com.playground.mqtt.qos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

    private final ConcurrentMap<String, Map<Integer, Qos1PacketState>> outboundStates =
            new ConcurrentHashMap<>();

    public boolean updateInboundState(String clientId, int packetId, Qos1PacketState state) {
        validate(clientId, packetId);
        return updateState(inboundStates, clientId, packetId, state);
    }

    public boolean updateOutboundState(String clientId, int packetId, Qos1PacketState state) {
        validate(clientId, packetId);
        return updateState(outboundStates, clientId, packetId, state);
    }

    public boolean cleanStatus(String clientId, int packetId, Qos1PacketState state) {
        validate(clientId, packetId);
        Map<Integer, Qos1PacketState> states = outboundStates.get(clientId);
        if (states == null) {
            return false;
        }
        Qos1PacketState result = states.compute(
                packetId,
                (ignored, prevState) -> prevState == state ? null : prevState
        );
        if (states.isEmpty()) {
            outboundStates.remove(clientId, states);
        }
        return result == null;
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
}
