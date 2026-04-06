package com.playground.mqtt.protocol.frame;

import java.nio.ByteBuffer;

public class MqttFrame {

    private final MqttPacketType packetType;
    private final ByteBuffer payload;

    public MqttFrame(MqttPacketType packetType, ByteBuffer payload) {
        this.packetType = packetType;
        this.payload = payload;
    }

    public MqttPacketType packetType() {
        return packetType;
    }

    public ByteBuffer payload() {
        return payload;
    }

    @Override
    public String toString() {
        if (payload == null) {
            return "MqttFrame{packetType=" + packetType + ", payload=null}";
        }
        return "MqttFrame{packetType=" + packetType
                + ", payload=ByteBuffer[pos=" + payload.position()
                + ", lim=" + payload.limit()
                + ", rem=" + payload.remaining()
                + "]}";
    }
}
