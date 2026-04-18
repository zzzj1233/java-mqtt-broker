package com.playground.mqtt.qos;

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

    private final ConcurrentMap<String, AtomicInteger> countersByClient = new ConcurrentHashMap<>();

    public int nextPacketId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId must not be blank");
        }
        AtomicInteger counter = countersByClient.computeIfAbsent(
                clientId,
                ignored -> new AtomicInteger(0)
        );
        while (true) {
            int current = counter.get();
            int next = current >= MAX_PACKET_ID ? 1 : current + 1;
            if (counter.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    public void clearClient(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return;
        }
        countersByClient.remove(clientId);
    }
}
