package com.playground.mqtt.protocol.codec;

import com.playground.mqtt.protocol.decode.*;
import com.playground.mqtt.protocol.encode.PacketEncoder;
import com.playground.mqtt.protocol.encode.PubAckEncoder;
import com.playground.mqtt.protocol.encode.PublishEncoder;
import com.playground.mqtt.protocol.frame.MqttFrame;
import com.playground.mqtt.protocol.frame.MqttPacketType;
import com.playground.mqtt.protocol.io.ByteBufferReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public final class DefaultMqttCodec implements MqttCodec {

    private final Map<MqttPacketType, PacketDecoder> decoders;

    private final Map<MqttPacketType, PacketEncoder> encoders;

    public DefaultMqttCodec() {
        Map<MqttPacketType, PacketDecoder> decoderMap = new HashMap<>();
        decoderMap.put(MqttPacketType.CONNECT, new ConnectPacketDecoder());
        decoderMap.put(MqttPacketType.SUBSCRIBE, new SubscribePacketDecoder());
        decoderMap.put(MqttPacketType.PUBLISH, new PublishPacketDecoder());
        decoderMap.put(MqttPacketType.PUBACK, new PubAckDecoder());

        Map<MqttPacketType, PacketEncoder> encoderMap = new HashMap<>();
        encoderMap.put(MqttPacketType.PUBLISH, new PublishEncoder());
        encoderMap.put(MqttPacketType.PUBACK, new PubAckEncoder());

        this.decoders = decoderMap;
        this.encoders = encoderMap;
    }

    @Override
    public MqttFrame tryDecode(ByteBuffer inBuffer) throws IOException {
        ByteBufferReader reader = new ByteBufferReader(inBuffer);
        return reader.readAtomically(() -> {
            DecodedPacket packet = readPacketEnvelope(reader);
            if (packet == null) {
                return null;
            }
            return decodeByType(packet);
        });
    }

    private MqttFrame decodeByType(DecodedPacket packet) {
        PacketDecoder decoder = decoders.get(packet.type());
        if (decoder == null) {
            return null;
        }
        return decoder.decode(packet);
    }

    private DecodedPacket readPacketEnvelope(ByteBufferReader reader) {
        Integer first = reader.readUint8();
        if (first == null) {
            return null;
        }
        int firstByte = first;

        int typeNibble = (firstByte & 0b11110000) >>> 4;
        int flags = firstByte & 0b00001111;
        MqttPacketType type = MqttPacketType.fromTypeNibble(typeNibble);

        Integer remainLength = readRemainLength(reader);
        if (remainLength == null) {
            return null;
        }

        if (!reader.checkReadable(remainLength)) {
            return null;
        }

        byte[] body = reader.readByteArray(remainLength);
        if (body == null) {
            return null;
        }
        return new DecodedPacket(type, flags, remainLength, ByteBuffer.wrap(body));
    }

    public Integer readRemainLength(ByteBufferReader reader) {

        int len = 0;

        for (int i = 0; i < 4; i++) {
            Integer oneByte = reader.readUint8();
            if (oneByte == null) {
                return null;
            }
            boolean inProgress = (oneByte & 0b10000000) > 0;
            len += (oneByte & 0b01111111) << (i * 7);
            if (!inProgress) {
                return len;
            }
            if (i == 3) {
                return null;
            }
        }
        return null;
    }

    @Override
    public ByteBuffer encode(MqttFrame frame) throws IOException {

        PacketEncoder packetEncoder = this.encoders.get(frame.packetType());

        if (packetEncoder == null)
            throw new IOException("No encoder for packet type: " + frame.packetType());

        return packetEncoder.encode(frame);
    }
}
