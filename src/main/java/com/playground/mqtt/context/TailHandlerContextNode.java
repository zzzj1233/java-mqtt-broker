package com.playground.mqtt.context;

public class TailHandlerContextNode extends HandlerContextNode{


    private static final ChannelInboundHandler TAIL_HANDLER = new ChannelInboundHandler() {
        // TODO: 兜底日志/资源释放
    };

    public TailHandlerContextNode(ChannelPipeline pipeline) {
        super(pipeline, "tail", TAIL_HANDLER);
    }

}
