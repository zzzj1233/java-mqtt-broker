package com.playground.mqtt.context;

public interface ChannelHandler {

    default void channelRead(ChannelContext ctx, Object msg) {
        ctx.fireChannelRead(msg);
    }
    default void exceptionCaught(ChannelContext ctx, Throwable cause) {
        ctx.fireExceptionCaught(cause);
    }
    default void channelInactive(ChannelContext ctx) {
        ctx.fireChannelInactive();
    }

}
