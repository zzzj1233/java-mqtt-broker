package com.playground.mqtt.qos;

/**
 * QoS1 packet lifecycle state for learning-oriented broker implementation.
 */
public enum Qos1PacketState {
    /**
     * Packet is tracked but not yet acknowledged by peer.
     */
    IN_FLIGHT(null),

    /**
     * Packet has been acknowledged and can be cleaned up.
     */
    ACKED(IN_FLIGHT);

    private final Qos1PacketState previousState;

    Qos1PacketState(Qos1PacketState previousState) {
        this.previousState = previousState;
    }

    public Qos1PacketState previousState() {
        return previousState;
    }

    public boolean canTransitionFrom(Qos1PacketState fromState) {
        return this.previousState == fromState;
    }
}
