package com.playground.mqtt.context;

import com.playground.mqtt.transport.channel.NioSocketChannel;

import java.nio.channels.SocketChannel;

public class HandlerContextNode implements ChannelContext {

    private final ChannelPipeline pipeline;
    private final String name;
    private final ChannelHandler handler;
    private HandlerContextNode prev;
    private HandlerContextNode next;

    public HandlerContextNode(ChannelPipeline pipeline, ChannelHandler handler) {
        this(pipeline, handler.getClass().getSimpleName(), handler);
    }

    public HandlerContextNode(ChannelPipeline pipeline, String name, ChannelHandler handler) {
        this.pipeline = pipeline;
        this.name = name;
        this.handler = handler;
    }

    @Override
    public SocketChannel channel() {
        return this.pipeline.socketChannel();
    }

    @Override
    public NioSocketChannel nioChannel() {
        ConnectionAttachment attachment = attachment();
        if (attachment != null && attachment.getNioSocketChannel() != null) {
            return attachment.getNioSocketChannel();
        }
        return new NioSocketChannel(channel(), attachment);
    }

    @Override
    public ConnectionAttachment attachment() {
        return this.pipeline.connectionAttachment();
    }

    @Override
    public void fireChannelRead(Object msg) {
        findNextInboundHandler().invokeChannelRead(msg);
    }

    @Override
    public void fireChannelWrite(Object msg) {
        findPreviousOutboundHandler().invokeChannelWrite(msg);
    }

    @Override
    public void fireExceptionCaught(Throwable cause) {
        findNextInboundHandler().invokeExceptionCaught(cause);
    }

    @Override
    public void fireChannelInactive() {
        findNextInboundHandler().invokeChannelInactive();
    }

    @Override
    public void writeAndFlush(Object msg) {
        fireChannelWrite(msg);
    }

    @Override
    public void close() {
        try {
            findPreviousOutboundHandler().close();
        } catch (IllegalStateException e) {
            try {
                SocketChannel ch = channel();
                if (ch != null && ch.isOpen()) {
                    ch.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private HandlerContextNode findPreviousOutboundHandler() {
        HandlerContextNode prev = this.prev;
        while (prev != null) {
            if (isOutboundHandler(prev)) {
                return prev;
            }
            prev = prev.prev;
        }
        throw new IllegalStateException("No outbound handler before context: " + name);
    }

    private HandlerContextNode findNextInboundHandler() {
        HandlerContextNode cursor = this.next;

        while (cursor != null) {
            if (isInboundHandler(cursor))
                return cursor;
            cursor = cursor.next;
        }
        throw new IllegalStateException("No inbound handler before context: " + name);
    }

    private void invokeChannelRead(Object msg) {
        try {
            handler.channelRead(this, msg);
        } catch (Throwable t) {
            invokeExceptionCaught(t);
        }
    }

    private void invokeChannelWrite(Object msg) {
        ChannelOutboundHandler outboundHandler = (ChannelOutboundHandler) handler;
        try {
            outboundHandler.channelWrite(this, msg);
        } catch (Throwable t) {
            invokeExceptionCaught(t);
        }
    }

    private void invokeExceptionCaught(Throwable cause) {
        try {
            handler.exceptionCaught(this, cause);
        } catch (Throwable ignored) {
            // Template stage: keep exception propagation best-effort.
        }
    }

    private void invokeChannelInactive() {
        try {
            handler.channelInactive(this);
        } catch (Throwable t) {
            invokeExceptionCaught(t);
        }
    }

    private boolean isOutboundHandler(HandlerContextNode node) {
        return node.handler instanceof ChannelOutboundHandler;
    }

    private boolean isInboundHandler(HandlerContextNode node) {
        return node.handler instanceof ChannelInboundHandler;
    }

    void setNext(HandlerContextNode node) {
        HandlerContextNode oldNext = this.next;
        this.next = node;
        if (node != null) {
            node.prev = this;
            node.next = oldNext;
        }
        if (oldNext != null) {
            oldNext.prev = node;
        }
    }

    void setPrevious(HandlerContextNode node) {
        HandlerContextNode oldPrev = this.prev;
        this.prev = node;
        if (node != null) {
            node.next = this;
            node.prev = oldPrev;
        }
        if (oldPrev != null) {
            oldPrev.next = node;
        }
    }
}
