package com.playground.mqtt.session;

import java.nio.channels.SocketChannel;
import java.time.Instant;

public final class ClientSession {

    private final String clientId;
    private final boolean cleanSession;
    private final int keepAliveSeconds;
    private final SocketChannel channel;
    private final Instant connectedAt;

    public ClientSession(
            String clientId,
            boolean cleanSession,
            int keepAliveSeconds,
            SocketChannel channel,
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

    public SocketChannel channel() {
        return channel;
    }

    public Instant connectedAt() {
        return connectedAt;
    }
}
