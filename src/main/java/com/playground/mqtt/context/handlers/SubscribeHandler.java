package com.playground.mqtt.context.handlers;

import com.playground.mqtt.context.ChannelContext;
import com.playground.mqtt.context.ChannelInboundHandler;
import com.playground.mqtt.protocol.frame.SubscribeMqttFrame;
import com.playground.mqtt.session.ClientSession;
import com.playground.mqtt.session.SessionStore;
import com.playground.mqtt.subscription.Subscription;
import com.playground.mqtt.subscription.SubscriptionStore;

import java.nio.ByteBuffer;
import java.util.Optional;

public class SubscribeHandler implements ChannelInboundHandler {

    private final SubscriptionStore subscriptionStore;

    private final SessionStore sessionStore;

    public SubscribeHandler(SubscriptionStore subscriptionStore, SessionStore sessionStore) {
        this.subscriptionStore = subscriptionStore;
        this.sessionStore = sessionStore;
    }

    @Override
    public void channelRead(ChannelContext ctx, Object msg) {

        if (!(msg instanceof SubscribeMqttFrame)){
            ctx.fireChannelRead(msg);
            return;
        }

        var channel = ctx.nioChannel();

        Optional<ClientSession> sessionOpt = sessionStore.findByChannel(channel);

        if (sessionOpt.isEmpty()) {
            // 未建立会话，拒绝或关闭连接
            return;
        }

        SubscribeMqttFrame subscribeMqttFrame = (SubscribeMqttFrame) msg;
        String clientId = sessionOpt.get().clientId();

        subscriptionStore.add(new Subscription(
                clientId,
                subscribeMqttFrame.getTopicFilter(),
                subscribeMqttFrame.getRequestedQos(),
                channel
        ));

        System.out.printf(
                "Send SUBACK to clientId=%s, packetId=%d, topic=%s, qos=%d%n",
                clientId,
                subscribeMqttFrame.getPacketId(),
                subscribeMqttFrame.getTopicFilter(),
                subscribeMqttFrame.getRequestedQos()
        );
        ctx.writeAndFlush(buildSubAck(subscribeMqttFrame.getPacketId(), 0x00));

    }

    private ByteBuffer buildSubAck(int packetId, int grantedQos) {
        byte[] bytes = new byte[] {
                (byte) 0x90,        // SUBACK fixed header
                (byte) 0x03,        // remaining length = 2(packetId) + 1(return code)
                (byte) ((packetId >> 8) & 0xFF),
                (byte) (packetId & 0xFF),
                (byte) (grantedQos & 0xFF) // 0x00/0x01/0x02/0x80
        };
        return ByteBuffer.wrap(bytes);
    }

}
