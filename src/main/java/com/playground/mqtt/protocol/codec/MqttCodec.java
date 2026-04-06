package com.playground.mqtt.protocol.codec;

import com.playground.mqtt.protocol.frame.MqttFrame;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface MqttCodec extends AutoCloseable {

    MqttFrame tryDecode(ByteBuffer inBuffer) throws IOException;

    ByteBuffer encode(MqttFrame frame) throws IOException;

    @Override
    default void close() throws IOException {
        // No-op by default.
    }
}
