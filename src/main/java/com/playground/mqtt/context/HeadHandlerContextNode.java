package com.playground.mqtt.context;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeadHandlerContextNode extends HandlerContextNode {
    private static final Logger LOG = LoggerFactory.getLogger(HeadHandlerContextNode.class);

    private final static ChannelHandler headChannelHandler = new ChannelOutboundHandler() {
        @Override
        public void channelWrite(ChannelContext ctx, Object msg) {
            if (!(msg instanceof ByteBuffer)) {
                ctx.fireExceptionCaught(new IllegalArgumentException("Head expects ByteBuffer"));
                return;
            }
            ByteBuffer buf = (ByteBuffer) msg;

            ctx.attachment().getOutboundQueue().add(buf);
        }

        @Override
        public void close(ChannelContext ctx) {
            try {
                ctx.attachment().getOutboundQueue().clear();
                ctx.nioChannel().rawChannel().close();
            } catch (IOException e) {
                LOG.warn("Failed to close channel: {}", ctx.channel(), e);
                ctx.fireExceptionCaught(e); // 可选，但建议保留
            }
        }
    };

    public HeadHandlerContextNode(ChannelPipeline pipeline) {
        super(pipeline, "head", headChannelHandler);
    }

}
