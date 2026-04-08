package com.playground.mqtt.context.handlers;

import com.playground.mqtt.context.ChannelContext;
import com.playground.mqtt.context.ChannelInboundHandler;
import com.playground.mqtt.context.ChannelPipeline;
import com.playground.mqtt.protocol.frame.PubAckMqttFrame;
import com.playground.mqtt.protocol.frame.PublishMqttFrame;
import com.playground.mqtt.subscription.Subscription;
import com.playground.mqtt.subscription.SubscriptionStore;

import java.util.List;

public class PublishHandler implements ChannelInboundHandler {

    private final SubscriptionStore subscriptionStore;

    public PublishHandler(SubscriptionStore subscriptionStore) {
        this.subscriptionStore = subscriptionStore;
    }

    @Override
    public void channelRead(ChannelContext ctx, Object msg) {

        if (msg instanceof PublishMqttFrame) {

            PublishMqttFrame frame = (PublishMqttFrame) msg;
            System.out.printf(
                    "PublishHandler received PUBLISH topic=%s qos=%d payloadBytes=%d from channel=%s%n",
                    frame.getTopic(),
                    frame.getQos(),
                    frame.payload() == null ? -1 : frame.payload().remaining(),
                    ctx.channel()
            );

            if (frame.getQos() == 1) {
                PubAckMqttFrame ackMqttFrame = new PubAckMqttFrame(frame.getPacketId());
                ctx.writeAndFlush(ackMqttFrame);
            }

            List<Subscription> subscriptions = subscriptionStore.matchTopic(frame.getTopic());

            if (subscriptions == null || subscriptions.isEmpty()) {
                System.out.printf(
                        "PublishHandler no matched subscriptions for topic=%s%n",
                        frame.getTopic()
                );
                return;
            }
            System.out.printf(
                    "PublishHandler matched %d subscriptions for topic=%s%n",
                    subscriptions.size(),
                    frame.getTopic()
            );

            for (Subscription subscription : subscriptions) {

                ChannelPipeline clientPipeline = subscription.channel().attachment().getChannelPipeline();
                System.out.printf(
                        "PublishHandler forwarding topic=%s to clientId=%s channel=%s%n",
                        frame.getTopic(),
                        subscription.clientId(),
                        subscription.channel()
                );

                clientPipeline.fireChannelWrite(
                        frame
                );

            }

        } else {
            ctx.fireChannelRead(msg);
        }

    }
}
