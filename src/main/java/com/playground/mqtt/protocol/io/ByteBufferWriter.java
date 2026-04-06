package com.playground.mqtt.protocol.io;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class ByteBufferWriter {

    private static final int MAX_REMAINING_LENGTH = 268435455;

    private final ByteBuffer buffer;

    public ByteBufferWriter(ByteBuffer buffer) {
        this.buffer = Objects.requireNonNull(buffer, "buffer");
    }

    public static ByteBufferWriter allocate(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        return new ByteBufferWriter(ByteBuffer.allocate(capacity));
    }

    public ByteBufferWriter writeUint8(int value) {
        if (value < 0 || value > 0xFF) {
            throw new IllegalArgumentException("uint8 out of range: " + value);
        }
        ensureWritable(1);
        buffer.put((byte) value);
        return this;
    }

    public ByteBufferWriter writeUint16(int value) {
        if (value < 0 || value > 0xFFFF) {
            throw new IllegalArgumentException("uint16 out of range: " + value);
        }
        ensureWritable(2);
        buffer.put((byte) ((value >>> 8) & 0xFF));
        buffer.put((byte) (value & 0xFF));
        return this;
    }

    public ByteBufferWriter writeByteArray(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");
        ensureWritable(bytes.length);
        buffer.put(bytes);
        return this;
    }

    public ByteBufferWriter writeMqttString(String value) {
        Objects.requireNonNull(value, "value");
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 0xFFFF) {
            throw new IllegalArgumentException("MQTT string too long: " + bytes.length);
        }
        return writeUint16(bytes.length).writeByteArray(bytes);
    }

    public ByteBufferWriter writeRemainingLength(int remainingLength) {
        if (remainingLength < 0 || remainingLength > MAX_REMAINING_LENGTH) {
            throw new IllegalArgumentException("remaining length out of range: " + remainingLength);
        }
        do {
            int encoded = remainingLength % 128;
            remainingLength = remainingLength / 128;
            if (remainingLength > 0) {
                encoded = encoded | 0x80;
            }
            writeUint8(encoded);
        } while (remainingLength > 0);
        return this;
    }

    public int position() {
        return buffer.position();
    }

    public int remaining() {
        return buffer.remaining();
    }

    public ByteBuffer buffer() {
        return buffer;
    }

    public ByteBuffer toReadBuffer() {
        ByteBuffer readBuffer = buffer.duplicate();
        readBuffer.flip();
        return readBuffer;
    }

    private void ensureWritable(int bytes) {
        if (buffer.remaining() < bytes) {
            throw new BufferOverflowException();
        }
    }
}
