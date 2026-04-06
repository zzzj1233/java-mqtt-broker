package com.playground.mqtt.context;

@FunctionalInterface
public interface ChannelPipelineInitializer {

    ChannelPipelineInitializer NOOP = pipeline -> {
    };

    void initChannel(ChannelPipeline pipeline);
}
