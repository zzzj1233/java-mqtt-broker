package com.playground.mqtt.context.handlers;

import com.playground.mqtt.context.ChannelContext;
import com.playground.mqtt.context.ChannelInboundHandler;
import com.playground.mqtt.protocol.frame.ConnectMqttFrame;
import com.playground.mqtt.session.ClientSession;
import com.playground.mqtt.session.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Instant;

public class ConnectionHandler implements ChannelInboundHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionHandler.class);

    private final SessionStore sessionStore;

    public ConnectionHandler(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public void channelRead(ChannelContext ctx, Object msg) {

        if (msg instanceof ConnectMqttFrame) {

            ConnectMqttFrame connectMqttFrame = (ConnectMqttFrame) msg;

            String clientId = connectMqttFrame.clientId();
            var channel = ctx.nioChannel();

            sessionStore.bind(new ClientSession(clientId, connectMqttFrame.cleanSession(), connectMqttFrame.keepAliveSeconds(), channel, Instant.now()));

            // CONNECT_ACK
            LOG.info("Send CONNACK to clientId={}, channel={}", clientId, ctx.channel());
            ctx.writeAndFlush(ByteBuffer.wrap(new byte[]{0x20, 0x02, 0x00, 0x00}));

            return;
        }

        ctx.fireChannelRead(msg);
    }

}
