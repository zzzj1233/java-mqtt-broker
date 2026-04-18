package com.playground.mqtt.context.handlers;

import com.playground.mqtt.context.ChannelContext;
import com.playground.mqtt.context.ChannelInboundHandler;
import com.playground.mqtt.context.ChannelPipeline;
import com.playground.mqtt.protocol.frame.PubAckMqttFrame;
import com.playground.mqtt.protocol.frame.PublishMqttFrame;
import com.playground.mqtt.qos.InMemoryPacketIdGenerator;
import com.playground.mqtt.qos.InMemoryQos1Store;
import com.playground.mqtt.qos.Qos1PacketState;
import com.playground.mqtt.session.ClientSession;
import com.playground.mqtt.session.SessionStore;
import com.playground.mqtt.subscription.Subscription;
import com.playground.mqtt.subscription.SubscriptionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class PublishHandler implements ChannelInboundHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PublishHandler.class);

    private final SubscriptionStore subscriptionStore;

    private final InMemoryQos1Store inMemoryQos1Store;

    private final InMemoryPacketIdGenerator packetIdGenerator;

    private final SessionStore sessionStore;

    public PublishHandler(
            SubscriptionStore subscriptionStore,
            InMemoryQos1Store inMemoryQos1Store,
            InMemoryPacketIdGenerator packetIdGenerator,
            SessionStore sessionStore
    ) {
        this.subscriptionStore = subscriptionStore;
        this.inMemoryQos1Store = inMemoryQos1Store;
        this.packetIdGenerator = packetIdGenerator;
        this.sessionStore = sessionStore;
    }

    @Override
    public void channelRead(ChannelContext ctx, Object msg) {

        if (msg instanceof PublishMqttFrame) {

            PublishMqttFrame frame = (PublishMqttFrame) msg;
            LOG.info(
                    "PublishHandler received PUBLISH topic={} qos={} payloadBytes={} from channel={}",
                    frame.getTopic(),
                    frame.getQos(),
                    frame.payload() == null ? -1 : frame.payload().remaining(),
                    ctx.channel()
            );

            if (frame.getQos() == 1) {

                // if (!inMemoryQos1Store.markFirstSeen(frame.))
                Optional<ClientSession> clientSession = sessionStore.findByChannel(ctx.nioChannel());

                if (clientSession.isEmpty()) {
                    ctx.close();
                    return;
                }

                String clientId = clientSession.get().clientId();

                if (inMemoryQos1Store.markFirstSeen(clientId, frame.getPacketId())) {
                    doPublish(frame);
                }

                ctx.writeAndFlush(new PubAckMqttFrame(frame.getPacketId()));
            } else {
                doPublish(frame);
            }

        } else {
            ctx.fireChannelRead(msg);
        }

    }

    private void doPublish(PublishMqttFrame frame) {
        List<Subscription> subscriptions = subscriptionStore.matchTopic(frame.getTopic());

        if (subscriptions == null || subscriptions.isEmpty()) {
            LOG.info("PublishHandler no matched subscriptions for topic={}", frame.getTopic());
            return;
        }
        LOG.info("PublishHandler matched {} subscriptions for topic={}", subscriptions.size(), frame.getTopic());

        int publishQos = frame.getQos();

        for (Subscription subscription : subscriptions) {

            ChannelPipeline clientPipeline = subscription.channel().attachment().getChannelPipeline();

            LOG.debug(
                    "PublishHandler forwarding topic={} to clientId={} channel={}",
                    frame.getTopic(),
                    subscription.clientId(),
                    subscription.channel()
            );

            int qos = subscription.qos();

            int min = Math.min(qos, publishQos);

            PublishMqttFrame outboundFrame = frame;
            if (min == 1) {
                int outboundPacketId = reserveOutboundPacketId(subscription.clientId());
                outboundFrame = new PublishMqttFrame(
                        frame.getDup(),
                        1,
                        frame.getTopic(),
                        outboundPacketId,
                        frame.payload().duplicate()
                );
            } else if (min == 2) {

                // TODO
            } else if (min == 0 && publishQos > 0) {
                outboundFrame = new PublishMqttFrame(
                        frame.getDup(),
                        0,
                        frame.getTopic(),
                        null,
                        frame.payload().duplicate()
                );
            }

            clientPipeline.fireChannelWrite(outboundFrame);

        }
    }

    private int reserveOutboundPacketId(String clientId) {
        for (int i = 0; i < 0xFFFF; i++) {
            int candidate = packetIdGenerator.nextPacketId(clientId);
            if (inMemoryQos1Store.updateOutboundState(clientId, candidate, Qos1PacketState.IN_FLIGHT)) {
                return candidate;
            }
        }
        throw new IllegalStateException("No available outbound packetId for clientId: " + clientId);
    }
}
