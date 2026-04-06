package com.playground.mqtt.protocol.decode;

import com.playground.mqtt.protocol.frame.SubscribeMqttFrame;
import com.playground.mqtt.protocol.io.ByteBufferReader;

public class SubscribePacketDecoder implements PacketDecoder {

    @Override
    public SubscribeMqttFrame decode(DecodedPacket packet) {

        if (packet.flags() != 0b0010) {
            return null;
        }

        ByteBufferReader reader = new ByteBufferReader(packet.body());

        // 1) Variable Header: Packet Identifier
        Integer packetId = reader.readUint16();
        if (packetId == null || packetId == 0) {
            return null; // 半包或非法 packetId
        }

        // 2) Payload: Topic Filter + Requested QoS
        String topicFilter = reader.readMQTTString();
        if (topicFilter == null) {
            return null;
        }

        Integer requestedQos = reader.readUint8();
        if (requestedQos == null) {
            return null;
        }

        return new SubscribeMqttFrame(packetId, topicFilter, requestedQos, packet.body());
    }

}
