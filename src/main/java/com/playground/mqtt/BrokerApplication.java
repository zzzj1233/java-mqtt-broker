package com.playground.mqtt;

import com.playground.mqtt.bootstrap.BrokerServer;
import com.playground.mqtt.config.BrokerConfig;
import com.playground.mqtt.context.ChannelContext;
import com.playground.mqtt.context.ChannelInboundHandler;
import com.playground.mqtt.context.ChannelPipelineFactory;
import com.playground.mqtt.context.DefaultChannelPipelineFactory;
import com.playground.mqtt.context.handlers.MqttCodecHandler;
import com.playground.mqtt.context.handlers.ConnectionHandler;
import com.playground.mqtt.context.handlers.PublishHandler;
import com.playground.mqtt.context.handlers.SubscribeHandler;
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

import java.io.IOException;

public final class BrokerApplication {

    private BrokerApplication() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        BrokerConfig config = BrokerConfig.fromEnv();
        Poller poller = new NioPoller();
        SocketAcceptor socketAcceptor = new NioSocketAcceptor();
        SessionStore sessionStore = new InMemorySessionStore();
        SubscriptionStore subscriptionStore = new InMemorySubscriptionStore();
        MessageRouter messageRouter = new DefaultMessageRouter();
        ChannelPipelineFactory pipelineFactory = new DefaultChannelPipelineFactory(
                pipeline -> {
                    pipeline.addLast(new MqttCodecHandler());
                    pipeline.addLast(new ConnectionHandler(sessionStore));
                    pipeline.addLast(new SubscribeHandler(subscriptionStore, sessionStore));
                    pipeline.addLast(new PublishHandler(subscriptionStore));
                    pipeline.addLast(new ChannelInboundHandler() {
                        @Override
                        public void channelRead(ChannelContext ctx, Object msg) {
                            System.out.println("Received: " + msg);
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
                pipelineFactory
        );

        Runtime.getRuntime().addShutdownHook(new Thread(brokerServer::stop));

        brokerServer.start();
        brokerServer.blockUntilShutdown();
    }
}
