package com.playground.mqtt.context;

import com.playground.mqtt.transport.channel.NioSocketChannel;

import java.nio.channels.SocketChannel;

public interface ChannelContext {

    SocketChannel channel();

    NioSocketChannel nioChannel();

    ConnectionAttachment attachment();

    void fireChannelRead(Object msg);

    void fireChannelWrite(Object msg);

    void fireExceptionCaught(Throwable cause);

    void fireChannelInactive();

    void fireChannelClose();

    void writeAndFlush(Object msg);

    void close();

}
