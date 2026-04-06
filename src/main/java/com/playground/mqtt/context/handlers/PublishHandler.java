package com.playground.mqtt.context.handlers;

import com.playground.mqtt.context.ChannelContext;
import com.playground.mqtt.context.ChannelInboundHandler;
import com.playground.mqtt.context.ChannelPipeline;
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

            List<Subscription> subscriptions = subscriptionStore.matchTopic(frame.getTopic());

            if (subscriptions == null || subscriptions.isEmpty()) {
                return;
            }

            for (Subscription subscription : subscriptions) {

                ChannelPipeline clientPipeline = subscription.channel().attachment().getChannelPipeline();

                clientPipeline.fireChannelWrite(
                        frame
                );

            }

        } else {
            ctx.fireChannelRead(msg);
        }

    }
}
