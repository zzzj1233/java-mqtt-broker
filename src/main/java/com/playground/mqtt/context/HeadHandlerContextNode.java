package com.playground.mqtt.context;

import java.nio.ByteBuffer;

public class HeadHandlerContextNode extends HandlerContextNode {

    private final static ChannelHandler headChannelHandler = new ChannelOutboundHandler() {
        @Override
        public void channelWrite(ChannelContext ctx, Object msg) {
            if (!(msg instanceof ByteBuffer)) {
                ctx.fireExceptionCaught(new IllegalArgumentException("Head expects ByteBuffer"));
                return;
            }
            ByteBuffer buf = (ByteBuffer) msg;
            try {
                ctx.channel().write(buf);
            } catch (java.io.IOException e) {
                ctx.fireExceptionCaught(e);
                ctx.close();
            }
        }
    };

    public HeadHandlerContextNode(ChannelPipeline pipeline) {
        super(pipeline, "head", headChannelHandler);
    }

}
