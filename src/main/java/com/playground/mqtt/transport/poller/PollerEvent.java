package com.playground.mqtt.transport.poller;

import java.nio.channels.SelectableChannel;

public final class PollerEvent {

    private final SelectableChannel channel;
    private final int readyOps;
    private final Object attachment;

    public PollerEvent(SelectableChannel channel, int readyOps, Object attachment) {
        this.channel = channel;
        this.readyOps = readyOps;
        this.attachment = attachment;
    }

    public SelectableChannel channel() {
        return channel;
    }

    public int readyOps() {
        return readyOps;
    }

    public Object attachment() {
        return attachment;
    }
}
