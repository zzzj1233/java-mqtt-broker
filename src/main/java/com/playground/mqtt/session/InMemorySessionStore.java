package com.playground.mqtt.session;

import com.playground.mqtt.transport.channel.NioSocketChannel;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemorySessionStore implements SessionStore {

    private final Map<String, ClientSession> clientSessions = new ConcurrentHashMap<>();

    private final Map<NioSocketChannel, ClientSession> channelSessions = new ConcurrentHashMap<>();

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
    public Optional<ClientSession> findByChannel(NioSocketChannel channel) {

        return Optional.ofNullable(channelSessions.get(channel));
    }

    @Override
    public void removeByChannel(NioSocketChannel channel) {

        ClientSession removed = channelSessions.remove(channel);

        if (removed != null)
            clientSessions.remove(removed.clientId());
    }
}
