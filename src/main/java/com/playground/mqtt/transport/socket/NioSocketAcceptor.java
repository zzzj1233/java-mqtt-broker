package com.playground.mqtt.transport.socket;

import com.playground.mqtt.config.BrokerConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public final class NioSocketAcceptor implements SocketAcceptor {

    @Override
    public ServerSocketChannel openServerChannel(BrokerConfig config) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(
                new InetSocketAddress(config.port()),
                config.backlog()
        );
        return serverSocketChannel;
    }

    @Override
    public SocketChannel accept(ServerSocketChannel serverChannel) throws IOException {
        return serverChannel.accept();
    }

    @Override
    public void configureClientChannel(SocketChannel clientChannel) throws IOException {
        clientChannel.configureBlocking(false);
        clientChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
    }
}
