package com.playground.mqtt.session;

import com.playground.mqtt.transport.channel.NioSocketChannel;

import java.io.IOException;
import java.util.Optional;

public interface SessionStore extends AutoCloseable {

    void bind(ClientSession session);

    Optional<ClientSession> findByClientId(String clientId);

    Optional<ClientSession> findByChannel(NioSocketChannel channel);

    void removeByChannel(NioSocketChannel channel);

    @Override
    default void close() throws IOException {
        // No-op by default.
    }
}
