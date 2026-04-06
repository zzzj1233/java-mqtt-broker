package com.playground.mqtt.protocol.decode;

import com.playground.mqtt.protocol.frame.MqttPacketType;

import java.nio.ByteBuffer;

public final class DecodedPacket {
    private final MqttPacketType type;
    private final int flags;
    private final int remainingLength;
    private final ByteBuffer body;

    public DecodedPacket(MqttPacketType type, int flags, int remainingLength, ByteBuffer body) {
        this.type = type;
        this.flags = flags;
        this.remainingLength = remainingLength;
        this.body = body;
    }

    public MqttPacketType type() {
        return type;
    }

    public int flags() {
        return flags;
    }

    public int remainingLength() {
        return remainingLength;
    }

    public ByteBuffer body() {
        return body;
    }
}
