package com.playground.mqtt.context.handlers;

import com.playground.mqtt.context.ChannelContext;
import com.playground.mqtt.context.ChannelInboundHandler;
import com.playground.mqtt.context.ChannelOutboundHandler;
import com.playground.mqtt.protocol.codec.DefaultMqttCodec;
import com.playground.mqtt.protocol.codec.MqttCodec;
import com.playground.mqtt.protocol.frame.MqttFrame;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MqttCodecHandler implements ChannelInboundHandler, ChannelOutboundHandler {

    private final MqttCodec codec;

    public MqttCodecHandler() {
        this.codec = new DefaultMqttCodec();
    }

    @Override
    public void channelRead(ChannelContext ctx, Object msg) {
        if (!(msg instanceof ByteBuffer)) {
            ctx.fireChannelRead(msg);
            return;
        }
        ByteBuffer in = (ByteBuffer) msg;
        try {
            MqttFrame frame = codec.tryDecode(in);
            if (frame != null) {
                System.out.printf(
                        "MqttCodecHandler decoded frame type=%s from channel=%s%n",
                        frame.packetType(),
                        ctx.channel()
                );
                ctx.fireChannelRead(frame);
            } else {
                System.out.printf(
                        "MqttCodecHandler decode incomplete/unsupported for channel=%s (buffer rem=%d)%n",
                        ctx.channel(),
                        in.remaining()
                );
            }
        } catch (IOException e) {
            ctx.fireExceptionCaught(e);
            ctx.close(); // 解码失败通常断开连接更安全
        } catch (RuntimeException e) {
            ctx.fireExceptionCaught(e);
            ctx.close();
        }
    }

    @Override
    public void channelWrite(ChannelContext ctx, Object msg) {
        if (msg instanceof MqttFrame) {
            try {
                ByteBuffer encoded = codec.encode((MqttFrame) msg);
                System.out.printf(
                        "MqttCodecHandler encoded frame type=%s bytes=%d for channel=%s%n",
                        ((MqttFrame) msg).packetType(),
                        encoded.remaining(),
                        ctx.channel()
                );
                ctx.fireChannelWrite(encoded);
            } catch (IOException e) {
                ctx.fireExceptionCaught(e);
                ctx.close(); // 解码失败通常断开连接更安全
            }
        } else {
            ctx.fireChannelWrite(msg);
        }
    }
}
