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
                int before = buf.remaining();
                int written = ctx.channel().write(buf);
                System.out.printf(
                        "Head write channel=%s written=%d requested=%d remainingAfter=%d%n",
                        ctx.channel(),
                        written,
                        before,
                        buf.remaining()
                );
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
