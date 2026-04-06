package com.playground.mqtt.context;

import java.nio.channels.SocketChannel;

public interface ChannelPipeline {

    void fireChannelRead(Object msg);

    void fireChannelWrite(Object msg);

    void addLast(ChannelHandler channelHandler);

    SocketChannel socketChannel();

    ConnectionAttachment connectionAttachment();

}
