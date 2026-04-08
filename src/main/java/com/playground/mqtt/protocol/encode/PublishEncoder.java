package com.playground.mqtt.protocol.encode;

import com.playground.mqtt.protocol.frame.MqttFrame;
import com.playground.mqtt.protocol.frame.MqttPacketType;
import com.playground.mqtt.protocol.frame.PublishMqttFrame;
import com.playground.mqtt.protocol.io.ByteBufferWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PublishEncoder implements PacketEncoder {

    @Override
    public ByteBuffer encode(MqttFrame frame) throws IOException {

        PublishMqttFrame publishFrame = (PublishMqttFrame) frame;

        ByteBuffer buffer = ByteBuffer.allocate(totalLength(publishFrame));

        ByteBufferWriter writer = new ByteBufferWriter(buffer);

        // 1. flags

        // 低 4 位 flags：
        // bit3：DUP
        // bit2..1：QoS（00/01/10，11 非法）
        // bit0：RETAIN

        int dup = publishFrame.getDup() > 0 ? 0b1000 : 0;

        int qos = publishFrame.getQos() << 1;

        writer.writeUint8(
                MqttPacketType.PUBLISH.typeNibble() << 4 | dup | qos
        );

        writer.writeRemainingLength(remainingLength(publishFrame));

        // 2. Variable Header
        writer.writeMqttString(publishFrame.getTopic());
        if (qos > 0) {
            writer.writeUint16(publishFrame.getPacketId());
        }
        // 3. Payload
        writer.writeByteArray(publishFrame.payloadBytes());

        return writer.toReadBuffer();
    }

    private int totalLength(
            PublishMqttFrame publishFrame
    ) {
        // 1: type(4:high) + flags(4:low)
        // encodedRemainingLengthBytes: 1~4
        // remainingLength

        int remainingLength = remainingLength(publishFrame);
        return 1 + encodedRemainingLengthBytes(remainingLength) + remainingLength;
    }

    // min: 1
    // max: 4
    private int encodedRemainingLengthBytes(int remainingLength) {
        if (remainingLength >= (1 << 21)) {
            return 4;
        }
        if (remainingLength >= (1 << 14)) {
            return 3;
        }
        if (remainingLength >= (1 << 7)) {
            return 2;
        }
        return 1;
    }

    private int remainingLength(PublishMqttFrame publishFrame) {
        // 2 + (utf-8)topic.length + (qos > 0 ? 2 : 0) +  payloadLength
        return 2 + publishFrame.getTopic().getBytes(StandardCharsets.UTF_8).length + (publishFrame.getQos() > 0 ? 2 : 0) + publishFrame.payload().remaining();
    }

}
