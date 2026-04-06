package com.playground.mqtt.context;

import java.nio.channels.SocketChannel;

public class DefaultChannelPipeline implements ChannelPipeline {

    private final SocketChannel socketChannel;
    private final ConnectionAttachment connectionAttachment;

    private HandlerContextNode head;
    private HandlerContextNode tail;

    public DefaultChannelPipeline(
            SocketChannel socketChannel,
            ConnectionAttachment connectionAttachment
    ) {
        this.socketChannel = socketChannel;
        this.connectionAttachment = connectionAttachment;

        head = new HeadHandlerContextNode(this);
        tail = new TailHandlerContextNode(this);
        head.setNext(tail);
        tail.setPrevious(head);
    }

    @Override
    public void fireChannelRead(Object msg) {
        head.fireChannelRead(msg);
    }

    @Override
    public void fireChannelWrite(Object msg) {
        tail.fireChannelWrite(msg);
    }

    @Override
    public void addLast(ChannelHandler channelHandler) {
        HandlerContextNode node = new HandlerContextNode(this, channelHandler);
        this.tail.setPrevious(node);
    }

    @Override
    public SocketChannel socketChannel() {
        return this.socketChannel;
    }

    @Override
    public ConnectionAttachment connectionAttachment() {
        return this.connectionAttachment;
    }

}
