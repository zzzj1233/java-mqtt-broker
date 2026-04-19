package com.playground.mqtt;

import com.playground.mqtt.bootstrap.BrokerServer;
import com.playground.mqtt.bootstrap.DefaultPeriodicTaskRunner;
import com.playground.mqtt.bootstrap.PeriodicTaskRunner;
import com.playground.mqtt.config.BrokerConfig;
import com.playground.mqtt.context.ChannelContext;
import com.playground.mqtt.context.ChannelInboundHandler;
import com.playground.mqtt.context.ChannelPipelineFactory;
import com.playground.mqtt.context.DefaultChannelPipelineFactory;
import com.playground.mqtt.context.handlers.*;
import com.playground.mqtt.qos.InMemoryPacketIdGenerator;
import com.playground.mqtt.qos.InMemoryQos1Store;
import com.playground.mqtt.qos.PublishStore;
import com.playground.mqtt.router.DefaultMessageRouter;
import com.playground.mqtt.router.MessageRouter;
import com.playground.mqtt.session.InMemorySessionStore;
import com.playground.mqtt.session.SessionStore;
import com.playground.mqtt.subscription.InMemorySubscriptionStore;
import com.playground.mqtt.subscription.SubscriptionStore;
import com.playground.mqtt.transport.poller.NioPoller;
import com.playground.mqtt.transport.poller.Poller;
import com.playground.mqtt.transport.socket.NioSocketAcceptor;
import com.playground.mqtt.transport.socket.SocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class BrokerApplication {
    private static final Logger LOG = LoggerFactory.getLogger(BrokerApplication.class);

    private BrokerApplication() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        BrokerConfig config = BrokerConfig.fromEnv();

        Poller poller = new NioPoller();

        SocketAcceptor socketAcceptor = new NioSocketAcceptor();

        SessionStore sessionStore = new InMemorySessionStore();

        SubscriptionStore subscriptionStore = new InMemorySubscriptionStore();

        MessageRouter messageRouter = new DefaultMessageRouter();

        InMemoryQos1Store qos1Store = new InMemoryQos1Store();

        InMemoryPacketIdGenerator packetIdGenerator = new InMemoryPacketIdGenerator();

        PublishStore publishStore = new PublishStore();

        PeriodicTaskRunner periodicTaskRunner = new DefaultPeriodicTaskRunner(
                qos1Store,
                sessionStore,
                publishStore,
                packetIdGenerator,
                config.qos1RetryIntervalSeconds(),
                config.qos1MaxRetryCount()
        );

        ChannelPipelineFactory pipelineFactory = new DefaultChannelPipelineFactory(
                pipeline -> {
                    pipeline.addLast(new MqttCodecHandler());
                    pipeline.addLast(new ConnectionHandler(sessionStore));
                    pipeline.addLast(new SubscribeHandler(subscriptionStore, sessionStore));
                    pipeline.addLast(new PublishHandler(subscriptionStore, qos1Store, packetIdGenerator, sessionStore, publishStore));
                    pipeline.addLast(new PubAckHandler(qos1Store, sessionStore, publishStore, packetIdGenerator));
                    pipeline.addLast(new ChannelInboundHandler() {
                        @Override
                        public void channelRead(ChannelContext ctx, Object msg) {
                            LOG.debug("Received: {}", msg);
                        }
                    });
                }
        );

        BrokerServer brokerServer = new BrokerServer(
                config,
                poller,
                socketAcceptor,
                sessionStore,
                subscriptionStore,
                messageRouter,
                pipelineFactory,
                periodicTaskRunner
        );

        Runtime.getRuntime().addShutdownHook(new Thread(brokerServer::stop));

        brokerServer.start();

        brokerServer.blockUntilShutdown();
    }
}
