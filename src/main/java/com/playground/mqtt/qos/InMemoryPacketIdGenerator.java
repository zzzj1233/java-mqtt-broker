package com.playground.mqtt.qos;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Per-client MQTT packet identifier generator.
 *
 * <p>Packet id range is 1..65535 and wraps around.
 */
public final class InMemoryPacketIdGenerator {

    private static final int MAX_PACKET_ID = 0xFFFF;

    private final ConcurrentMap<String, Set<Integer>> packetIds = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, AtomicInteger> countersByClient = new ConcurrentHashMap<>();

    public int nextPacketId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId must not be blank");
        }
        if (packetIds.getOrDefault(clientId, Collections.emptySet()).size() >= MAX_PACKET_ID) {
            throw new IllegalStateException("clientId is fulled can not allocated more than " + MAX_PACKET_ID);
        }
        AtomicInteger counter = countersByClient.computeIfAbsent(
                clientId,
                ignored -> new AtomicInteger(0)
        );
        int next = -1;

        int loop = 0;

        while (loop < MAX_PACKET_ID) {

            int current = counter.get();

            next = current >= MAX_PACKET_ID ? 1 : current + 1;

            if (!packetIds.getOrDefault(clientId, Collections.emptySet()).contains(next) && counter.compareAndSet(current, next)) {
                break;
            }

            loop++;
        }

        if (loop >= MAX_PACKET_ID) {
            throw new IllegalStateException("clientId is fulled can not allocated more than " + MAX_PACKET_ID);
        }

        packetIds.computeIfAbsent(clientId, ignored -> ConcurrentHashMap.newKeySet()).add(next);

        return next;
    }

    public boolean cleanPacketId(String clientId, Integer packageId) {

        return packetIds.getOrDefault(clientId, Collections.emptySet()).remove(packageId);
    }

    public void clearClient(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return;
        }
        countersByClient.remove(clientId);
        packetIds.remove(clientId);
    }
}
