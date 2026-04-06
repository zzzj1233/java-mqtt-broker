package com.playground.mqtt.transport.channel;

import com.playground.mqtt.context.ConnectionAttachment;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;

public final class NioSocketChannel {

    private final SocketChannel rawChannel;
    private ConnectionAttachment connectionAttachment;

    public NioSocketChannel(SocketChannel rawChannel) {
        this(rawChannel, null);
    }

    public NioSocketChannel(SocketChannel rawChannel, ConnectionAttachment connectionAttachment) {
        this.rawChannel = Objects.requireNonNull(rawChannel, "rawChannel");
        this.connectionAttachment = connectionAttachment;
    }

    public SocketChannel rawChannel() {
        return rawChannel;
    }

    public int write(ByteBuffer src) throws IOException {
        return rawChannel.write(src);
    }

    public ConnectionAttachment attachment() {
        return connectionAttachment;
    }

    public void setAttachment(ConnectionAttachment connectionAttachment) {
        this.connectionAttachment = connectionAttachment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NioSocketChannel)) {
            return false;
        }
        NioSocketChannel that = (NioSocketChannel) o;
        return rawChannel.equals(that.rawChannel);
    }

    @Override
    public int hashCode() {
        return rawChannel.hashCode();
    }

    @Override
    public String toString() {
        return "NioSocketChannel{" + rawChannel + "}";
    }
}
