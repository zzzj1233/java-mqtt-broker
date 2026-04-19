package com.playground.mqtt.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TailHandlerContextNode extends HandlerContextNode{

    private static final Logger LOG = LoggerFactory.getLogger(TailHandlerContextNode.class);

    private static final ChannelInboundHandler TAIL_HANDLER = new ChannelInboundHandler() {
        @Override
        public void exceptionCaught(ChannelContext ctx, Throwable cause) {
            LOG.error("Unhandled pipeline exception on channel={}", ctx.channel(), cause);
        }
    };

    public TailHandlerContextNode(ChannelPipeline pipeline) {
        super(pipeline, "tail", TAIL_HANDLER);
    }

}
