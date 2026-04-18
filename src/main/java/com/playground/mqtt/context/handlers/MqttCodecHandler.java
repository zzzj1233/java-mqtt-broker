package com.playground.mqtt.context.handlers;

import com.playground.mqtt.context.ChannelContext;
import com.playground.mqtt.context.ChannelInboundHandler;
import com.playground.mqtt.context.ChannelOutboundHandler;
import com.playground.mqtt.protocol.codec.DefaultMqttCodec;
import com.playground.mqtt.protocol.codec.MqttCodec;
import com.playground.mqtt.protocol.frame.MqttFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MqttCodecHandler implements ChannelInboundHandler, ChannelOutboundHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MqttCodecHandler.class);

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
                LOG.debug("MqttCodecHandler decoded frame type={} from channel={}", frame.packetType(), ctx.channel());
                ctx.fireChannelRead(frame);
            } else {
                LOG.debug("MqttCodecHandler decode incomplete/unsupported for channel={} (buffer rem={})",
                        ctx.channel(), in.remaining());
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
                LOG.debug("MqttCodecHandler encoded frame type={} bytes={} for channel={}",
                        ((MqttFrame) msg).packetType(), encoded.remaining(), ctx.channel());
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
