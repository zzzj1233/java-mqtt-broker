package com.playground.mqtt.protocol.decode;

import com.playground.mqtt.protocol.frame.MqttFrame;
import com.playground.mqtt.protocol.frame.PublishMqttFrame;
import com.playground.mqtt.protocol.io.ByteBufferReader;

import java.nio.ByteBuffer;

public class PublishPacketDecoder implements PacketDecoder {

    @Override
    public MqttFrame decode(DecodedPacket packet) {

        int flags = packet.flags();

        // 低 4 位 flags：
        // bit3：DUP
        // bit2..1：QoS（00/01/10，11 非法）
        // bit0：RETAIN

        int dup = (flags >>> 3) & 0b01;

        // 00000000
        int qos = (flags >>> 1) & 0b11;

        if (qos == 3) {
            return null;
        }

        ByteBufferReader reader = new ByteBufferReader(packet.body());

        String topic = reader.readMQTTString();

        if (topic == null) {
            return null;
        }

        Integer packetId = null;

        if (qos > 0) {
            if (!reader.checkReadable(2))
                return null;
            packetId = reader.readUint16();
        }

        return new PublishMqttFrame(
                dup,
                qos,
                topic,
                packetId,
                ByteBuffer.wrap(reader.readRemainingByteArray())
        );
    }

}
