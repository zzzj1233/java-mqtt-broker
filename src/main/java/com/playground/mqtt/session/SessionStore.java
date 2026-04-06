package com.playground.mqtt.session;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Optional;

public interface SessionStore extends AutoCloseable {

    void bind(ClientSession session);

    Optional<ClientSession> findByClientId(String clientId);

    Optional<ClientSession> findByChannel(SocketChannel channel);

    void removeByChannel(SocketChannel channel);

    @Override
    default void close() throws IOException {
        // No-op by default.
    }
}
