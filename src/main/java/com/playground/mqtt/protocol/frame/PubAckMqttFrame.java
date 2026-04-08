package com.playground.mqtt.protocol.frame;

public class PubAckMqttFrame extends MqttFrame {

    public final int packetId;

    public PubAckMqttFrame(int packetId) {
        super(MqttPacketType.PUBACK, null);
        this.packetId = packetId;
    }

    public int getPacketId() {
        return packetId;
    }
}
