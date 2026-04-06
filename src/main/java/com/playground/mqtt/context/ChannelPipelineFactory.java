package com.playground.mqtt.context;

import java.nio.channels.SocketChannel;

public interface ChannelPipelineFactory {

    ChannelPipeline createChannelPipeline(
        SocketChannel socketChannel,
        ConnectionAttachment connectionAttachment
    );

}
