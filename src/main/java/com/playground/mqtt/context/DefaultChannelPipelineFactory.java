package com.playground.mqtt.context;

import java.nio.channels.SocketChannel;

public class DefaultChannelPipelineFactory implements ChannelPipelineFactory{

    private final ChannelPipelineInitializer initializer;

    public DefaultChannelPipelineFactory(ChannelPipelineInitializer initializer) {
        this.initializer = initializer;
    }

    @Override
    public ChannelPipeline createChannelPipeline(
            SocketChannel socketChannel,
            ConnectionAttachment connectionAttachment) {

        DefaultChannelPipeline pipeline = new DefaultChannelPipeline(
                socketChannel,
                connectionAttachment
        );
        initializer.initChannel(pipeline);

        return pipeline;
    }

}
