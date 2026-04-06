package com.playground.mqtt.protocol.decode;

import com.playground.mqtt.protocol.frame.ConnectMqttFrame;
import com.playground.mqtt.protocol.io.ByteBufferReader;

import java.nio.ByteBuffer;

public final class ConnectPacketDecoder implements PacketDecoder {

    @Override
    public ConnectMqttFrame decode(DecodedPacket packet) {
        // CONNECT fixed header flags must be 0.
        if (packet.flags() != 0) {
            return null;
        }
        // CONNECT variable header is 10 bytes in MQTT 3.1.1.
        if (packet.remainingLength() < 10) {
            return null;
        }

        ByteBufferReader payloadReader = new ByteBufferReader(packet.body());
        String protocol = payloadReader.readMQTTString();
        if (!"MQTT".equals(protocol)) {
            return null;
        }

        Integer protocolLevel = payloadReader.readUint8();
        if (protocolLevel == null || protocolLevel != 0x04) {
            return null;
        }

        Integer connectionFlags = payloadReader.readUint8();
        if (connectionFlags == null) {
            return null;
        }
        // Reserved bit (bit0) must be 0 in MQTT 3.1.1 CONNECT.
        if ((connectionFlags & 0x01) != 0) {
            return null;
        }

        Integer keepalive = payloadReader.readUint16();
        if (keepalive == null) {
            return null;
        }

        int payloadLength = packet.remainingLength() - 10;
        byte[] payload = payloadReader.readByteArray(payloadLength);
        if (payload == null) {
            return null;
        }

        ByteBufferReader connectPayloadReader = new ByteBufferReader(ByteBuffer.wrap(payload));
        String clientId = connectPayloadReader.readMQTTString();
        if (clientId == null) {
            return null;
        }

        boolean cleanSession = (connectionFlags & 0x02) != 0;
        if (clientId.isEmpty() && !cleanSession) {
            return null;
        }

        return new ConnectMqttFrame(
                ByteBuffer.wrap(payload),
                protocol,
                protocolLevel,
                connectionFlags,
                keepalive,
                clientId);
    }
}
