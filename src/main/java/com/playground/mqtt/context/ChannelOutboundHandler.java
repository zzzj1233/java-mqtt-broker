package com.playground.mqtt.context;

public interface ChannelOutboundHandler extends ChannelHandler {

    default void channelWrite(ChannelContext ctx, Object msg) {
        ctx.fireChannelWrite(msg);
    }

}
