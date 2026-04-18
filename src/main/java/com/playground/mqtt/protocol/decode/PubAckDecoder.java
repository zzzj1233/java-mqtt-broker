package com.playground.mqtt.protocol.decode;

import com.playground.mqtt.protocol.frame.PubAckMqttFrame;
import com.playground.mqtt.protocol.io.ByteBufferReader;

public class PubAckDecoder implements PacketDecoder {

    @Override
    public PubAckMqttFrame decode(DecodedPacket packet) {
        if (packet.remainingLength() != 2) {
            return null;
        }
        if (packet.flags() != 0) {
            return null;
        }

        ByteBufferReader reader = new ByteBufferReader(packet.body());
        Integer packetId = reader.readUint16();
        if (packetId == null || packetId == 0) {
            return null;
        }

        return new PubAckMqttFrame(packetId);
    }

}
