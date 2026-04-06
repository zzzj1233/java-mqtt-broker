package com.playground.mqtt.subscription;

import java.nio.channels.SocketChannel;

public final class Subscription {

    private final String clientId;
    private final String topicFilter;
    private final int qos;
    private final SocketChannel channel;

    public Subscription(String clientId, String topicFilter, int qos, SocketChannel channel) {
        this.clientId = clientId;
        this.topicFilter = topicFilter;
        this.qos = qos;
        this.channel = channel;
    }

    public String clientId() {
        return clientId;
    }

    public int qos() {
        return qos;
    }

    public String topicFilter() {
        return topicFilter;
    }

    public SocketChannel channel() {
        return channel;
    }
}
