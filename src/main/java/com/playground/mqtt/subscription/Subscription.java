package com.playground.mqtt.subscription;

import com.playground.mqtt.transport.channel.NioSocketChannel;

public final class Subscription {

    private final String clientId;
    private final String topicFilter;
    private final int qos;
    private final NioSocketChannel channel;

    public Subscription(String clientId, String topicFilter, int qos, NioSocketChannel channel) {
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

    public NioSocketChannel channel() {
        return channel;
    }
}
