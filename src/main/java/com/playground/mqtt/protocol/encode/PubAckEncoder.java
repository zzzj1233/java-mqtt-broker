package com.playground.mqtt.protocol.encode;

import com.playground.mqtt.protocol.frame.MqttFrame;
import com.playground.mqtt.protocol.frame.MqttPacketType;
import com.playground.mqtt.protocol.frame.PubAckMqttFrame;
import com.playground.mqtt.protocol.io.ByteBufferWriter;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PubAckEncoder implements PacketEncoder{

    @Override
    public ByteBuffer encode(MqttFrame frame) throws IOException {

        // 1: type + flags    = 1
        // 2: remainingLength = 1
        // 3: packetId        = 2
        int totalLength = 4;

        ByteBufferWriter writer = new ByteBufferWriter(ByteBuffer.allocate(totalLength));

        writer.writeUint8(
                MqttPacketType.PUBACK.typeNibble() << 4
        );

        writer.writeRemainingLength(2);

        PubAckMqttFrame ackMqttFrame = (PubAckMqttFrame) frame;

        writer.writeUint16(ackMqttFrame.getPacketId());

        return writer.toReadBuffer();
    }

}
