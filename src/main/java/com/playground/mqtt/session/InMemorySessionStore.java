package com.playground.mqtt.session;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemorySessionStore implements SessionStore {

    private final Map<String, ClientSession> clientSessions = new ConcurrentHashMap<>();

    private final Map<SocketChannel, ClientSession> channelSessions = new ConcurrentHashMap<>();

    @Override
    public void bind(ClientSession session) {
        clientSessions.put(session.clientId(), session);
        channelSessions.put(session.channel(), session);
    }

    @Override
    public Optional<ClientSession> findByClientId(String clientId) {

        return Optional.ofNullable(clientSessions.get(clientId));
    }

    @Override
    public Optional<ClientSession> findByChannel(SocketChannel channel) {

        return Optional.ofNullable(channelSessions.get(channel));
    }

    @Override
    public void removeByChannel(SocketChannel channel) {

        ClientSession removed = channelSessions.remove(channel);

        if (removed != null)
            clientSessions.remove(removed.clientId());
    }
}
