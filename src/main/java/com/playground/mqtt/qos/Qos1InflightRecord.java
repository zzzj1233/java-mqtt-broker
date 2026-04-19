package com.playground.mqtt.qos;

/**
 * In-memory QoS1 inflight record for retry management.
 *
 * <p>This definition is intentionally small and focused:
 * - state: current lifecycle state
 * - firstSeenAtNanos: when broker first tracked this packet
 * - lastSentAtNanos: last send/retry timestamp
 * - retryCount: number of retries after first send
 */
public final class Qos1InflightRecord {

    private final Qos1PacketState state;
    private final long firstSeenAtNanos;
    private final long lastSentAtNanos;
    private final int retryCount;
    private final RefCountedPublishPayloadId publishPayloadId;

    private Qos1InflightRecord(
            Qos1PacketState state,
            long firstSeenAtNanos,
            long lastSentAtNanos,
            int retryCount,
            RefCountedPublishPayloadId publishPayloadId
    ) {
        this.state = state;
        this.firstSeenAtNanos = firstSeenAtNanos;
        this.lastSentAtNanos = lastSentAtNanos;
        this.retryCount = retryCount;
        this.publishPayloadId = publishPayloadId;
    }

    public static Qos1InflightRecord firstSend(long nowNanos, RefCountedPublishPayloadId payloadId) {
        return new Qos1InflightRecord(Qos1PacketState.IN_FLIGHT, nowNanos, nowNanos, 0, payloadId);
    }

    public Qos1InflightRecord retried(long nowNanos) {
        return new Qos1InflightRecord(this.state, this.firstSeenAtNanos, nowNanos, this.retryCount + 1, this.publishPayloadId);
    }

    public Qos1InflightRecord toAcked(long nowNanos) {
        return new Qos1InflightRecord(
                Qos1PacketState.ACKED,
                this.firstSeenAtNanos,
                nowNanos,
                this.retryCount,
                this.publishPayloadId
        );
    }

    public Qos1PacketState state() {
        return state;
    }

    public long firstSeenAtNanos() {
        return firstSeenAtNanos;
    }

    public long lastSentAtNanos() {
        return lastSentAtNanos;
    }

    public int retryCount() {
        return retryCount;
    }

    public RefCountedPublishPayloadId publishPayloadId() {
        return publishPayloadId;
    }
}
