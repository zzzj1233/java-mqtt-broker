package com.playground.mqtt.session;

import com.playground.mqtt.transport.channel.NioSocketChannel;

import java.time.Instant;

public final class ClientSession {

    private final String clientId;
    private final boolean cleanSession;
    private final int keepAliveSeconds;
    private final NioSocketChannel channel;
    private final Instant connectedAt;

    public ClientSession(
            String clientId,
            boolean cleanSession,
            int keepAliveSeconds,
            NioSocketChannel channel,
            Instant connectedAt
    ) {
        this.clientId = clientId;
        this.cleanSession = cleanSession;
        this.keepAliveSeconds = keepAliveSeconds;
        this.channel = channel;
        this.connectedAt = connectedAt;
    }

    public String clientId() {
        return clientId;
    }

    public boolean cleanSession() {
        return cleanSession;
    }

    public int keepAliveSeconds() {
        return keepAliveSeconds;
    }

    public NioSocketChannel channel() {
        return channel;
    }

    public Instant connectedAt() {
        return connectedAt;
    }
}
