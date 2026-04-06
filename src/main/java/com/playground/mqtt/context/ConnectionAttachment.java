package com.playground.mqtt.context;

import com.playground.mqtt.transport.channel.NioSocketChannel;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

public class ConnectionAttachment {

    final ByteBuffer readBuffer = ByteBuffer.allocate(8 * 1024);

    final Deque<ByteBuffer> outboundQueue = new ArrayDeque<>();

    String clientId;

    long lastActiveNanos;

    boolean closing;

    NioSocketChannel nioSocketChannel;

    ChannelPipeline channelPipeline;

    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }

    public Deque<ByteBuffer> getOutboundQueue() {
        return outboundQueue;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public long getLastActiveNanos() {
        return lastActiveNanos;
    }

    public void setLastActiveNanos(long lastActiveNanos) {
        this.lastActiveNanos = lastActiveNanos;
    }

    public boolean isClosing() {
        return closing;
    }

    public void setClosing(boolean closing) {
        this.closing = closing;
    }

    public ChannelPipeline getChannelPipeline() {
        return channelPipeline;
    }

    public void setChannelPipeline(ChannelPipeline channelPipeline) {
        this.channelPipeline = channelPipeline;
    }

    public NioSocketChannel getNioSocketChannel() {
        return nioSocketChannel;
    }

    public void setNioSocketChannel(NioSocketChannel nioSocketChannel) {
        this.nioSocketChannel = nioSocketChannel;
        if (nioSocketChannel != null) {
            nioSocketChannel.setAttachment(this);
        }
    }
}
