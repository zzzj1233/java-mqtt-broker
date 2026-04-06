package com.playground.mqtt.protocol.io;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Supplier;

public final class ByteBufferReader {
    private final ByteBuffer buffer;

    public ByteBufferReader(ByteBuffer buffer) {
        this.buffer = Objects.requireNonNull(buffer, "buffer");
    }

    public Integer readUint8() {
        if (buffer.remaining() < 1) {
            return null;
        }
        return buffer.get() & 0xFF;
    }

    public Integer readUint16() {
        if (buffer.remaining() < 2) {
            return null;
        }
        int hi = buffer.get() & 0xFF;
        int lo = buffer.get() & 0xFF;
        return (hi << 8) | lo;
    }

    public Long readUint32() {
        if (buffer.remaining() < 4) {
            return null;
        }
        long b1 = buffer.get() & 0xFFL;
        long b2 = buffer.get() & 0xFFL;
        long b3 = buffer.get() & 0xFFL;
        long b4 = buffer.get() & 0xFFL;
        long value = (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
        return value;
    }

    public Long readUint64() {
        if (buffer.remaining() < 8) {
            return null;
        }
        long value = 0L;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (buffer.get() & 0xFFL);
        }
        return value;
    }

    public byte[] readByteArray(int len) {
        if (len < 0) {
            throw new IllegalArgumentException("len must be >= 0");
        }
        if (buffer.remaining() < len) {
            return null;
        }
        byte[] data = new byte[len];
        buffer.get(data);
        return data;
    }

    public byte[] readRemainingByteArray() {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        return data;
    }

    public String readMQTTString() {
        return readAtomically(() -> {
            Integer len = readUint16();
            if (len == null) {
                return null;
            }
            byte[] data = readByteArray(len);
            if (data == null) {
                return null;
            }
            return new String(data, StandardCharsets.UTF_8);
        });
    }

    public <T> T readAtomically(Supplier<T> action) {
        Objects.requireNonNull(action, "action");

        int startPos = buffer.position();
        try {
            T result = action.get();
            if (result == null) {
                buffer.position(startPos);
            }
            return result;
        } catch (RuntimeException ex) {
            buffer.position(startPos);
            throw ex;
        }
    }

    public boolean checkReadable(int length) {
        return buffer.remaining() >= length;
    }
}
