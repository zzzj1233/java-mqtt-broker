package com.playground.mqtt.context;

import java.nio.channels.SocketChannel;

public interface ChannelContext {

    SocketChannel channel();

    ConnectionAttachment attachment();

    void fireChannelRead(Object msg);

    void fireChannelWrite(Object msg);

    void fireExceptionCaught(Throwable cause);

    void fireChannelInactive();

    void writeAndFlush(Object msg);

    void close();

}
