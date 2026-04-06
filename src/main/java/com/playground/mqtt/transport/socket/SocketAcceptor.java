package com.playground.mqtt.transport.socket;

import com.playground.mqtt.config.BrokerConfig;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public interface SocketAcceptor extends AutoCloseable {

    ServerSocketChannel openServerChannel(BrokerConfig config) throws IOException;

    SocketChannel accept(ServerSocketChannel serverChannel) throws IOException;

    void configureClientChannel(SocketChannel clientChannel) throws IOException;

    @Override
    default void close() throws IOException {
        // No-op by default.
    }
}
