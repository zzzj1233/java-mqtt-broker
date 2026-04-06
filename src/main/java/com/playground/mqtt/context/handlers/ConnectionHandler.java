package com.playground.mqtt.context.handlers;

import com.playground.mqtt.context.ChannelContext;
import com.playground.mqtt.context.ChannelInboundHandler;
import com.playground.mqtt.protocol.frame.ConnectMqttFrame;
import com.playground.mqtt.session.ClientSession;
import com.playground.mqtt.session.SessionStore;

import java.nio.ByteBuffer;
import java.time.Instant;

public class ConnectionHandler implements ChannelInboundHandler {

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
            System.out.printf("Send CONNACK to clientId=%s, channel=%s%n", clientId, ctx.channel());
            ctx.writeAndFlush(ByteBuffer.wrap(new byte[]{0x20, 0x02, 0x00, 0x00}));

            return;
        }

        ctx.fireChannelRead(msg);
    }

}
