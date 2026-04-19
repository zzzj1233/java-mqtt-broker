package com.playground.mqtt.bootstrap;

import com.playground.mqtt.config.BrokerConfig;
import com.playground.mqtt.context.ConnectionAttachment;
import com.playground.mqtt.context.ChannelPipeline;
import com.playground.mqtt.context.ChannelPipelineFactory;
import com.playground.mqtt.router.MessageRouter;
import com.playground.mqtt.session.SessionStore;
import com.playground.mqtt.subscription.SubscriptionStore;
import com.playground.mqtt.transport.channel.NioSocketChannel;
import com.playground.mqtt.transport.poller.Poller;
import com.playground.mqtt.transport.poller.PollerEvent;
import com.playground.mqtt.transport.socket.SocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class BrokerServer {
    private static final Logger LOG = LoggerFactory.getLogger(BrokerServer.class);
    private final BrokerConfig config;
    private final Poller poller;
    private final SocketAcceptor socketAcceptor;
    private final SessionStore sessionStore;
    private final SubscriptionStore subscriptionStore;
    private final MessageRouter messageRouter;
    private final CountDownLatch stopLatch = new CountDownLatch(1);
    private final ChannelPipelineFactory channelPipelineFactory;
    private final PeriodicTaskRunner periodicTaskRunner;

    private volatile boolean running;

    public BrokerServer(
            BrokerConfig config,
            Poller poller,
            SocketAcceptor socketAcceptor,
            SessionStore sessionStore,
            SubscriptionStore subscriptionStore,
            MessageRouter messageRouter,
            ChannelPipelineFactory channelPipelineFactory,
            PeriodicTaskRunner periodicTaskRunner
    ) {
        this.config = config;
        this.poller = poller;
        this.socketAcceptor = socketAcceptor;
        this.sessionStore = sessionStore;
        this.subscriptionStore = subscriptionStore;
        this.messageRouter = messageRouter;
        this.channelPipelineFactory = channelPipelineFactory;
        this.periodicTaskRunner = periodicTaskRunner;
    }

    public void start() throws IOException {
        running = true;
        LOG.info("NIO template broker started on port {} (pollTimeoutMs={})", config.port(), config.pollTimeoutMs());
        ServerSocketChannel serverSocketChannel = socketAcceptor.openServerChannel(config);
        poller.register(serverSocketChannel, SelectionKey.OP_ACCEPT, null);

        while (running) {
            List<PollerEvent> events;
            try {
                events = poller.poll(config.pollTimeoutMs());
            } catch (IOException pollError) {
                LOG.error("Poller failure, stopping broker", pollError);
                stop();
                break;
            }

            if (events == null || events.isEmpty()) {
                periodicTaskRunner.runOnce();
                continue;
            }


            handleEvents(events);
            periodicTaskRunner.runOnce();
        }

    }

    public void handleEvents(List<PollerEvent> events) {
        for (PollerEvent event : events) {
            try {
                dispatchEvent(event);
            } catch (Exception eventError) {
                LOG.warn("Event handling failure", eventError);
                safeCloseEventChannel(event);
            }
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        stopLatch.await();
    }

    public void stop() {
        running = false;
        stopLatch.countDown();
        closeQuietly(subscriptionStore);
        closeQuietly(sessionStore);
        closeQuietly(socketAcceptor);
        closeQuietly(poller);
    }

    public boolean isRunning() {
        return running;
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
            // Template stage: keep shutdown resilient and simple.
        }
    }

    private void dispatchEvent(PollerEvent event) throws IOException {
        if (event == null) {
            return;
        }

        SelectableChannel channel = event.channel();
        if (channel == null) {
            return;
        }

        int readyOps = event.readyOps();
        if ((readyOps & SelectionKey.OP_ACCEPT) != 0) {
            handleAcceptEvent(channel);
            return;
        }
        if ((readyOps & SelectionKey.OP_READ) != 0) {
            handleReadEvent(channel, event.attachment());
        }
        if ((readyOps & SelectionKey.OP_WRITE) != 0) {
            handleWriteEvent(channel, event.attachment());
        }
    }

    private void handleAcceptEvent(SelectableChannel channel) throws IOException {
        if (!(channel instanceof ServerSocketChannel)) {
            return;
        }
        ServerSocketChannel serverChannel = (ServerSocketChannel) channel;
        while (running) {
            SocketChannel client = socketAcceptor.accept(serverChannel);
            if (client == null) {
                return;
            }
            socketAcceptor.configureClientChannel(client);

            ConnectionAttachment attachment = new ConnectionAttachment();
            NioSocketChannel nioSocketChannel = new NioSocketChannel(client, attachment);
            attachment.setNioSocketChannel(nioSocketChannel);

            ChannelPipeline channelPipeline = channelPipelineFactory.createChannelPipeline(client, attachment);

            attachment.setChannelPipeline(channelPipeline);

            poller.register(client, SelectionKey.OP_READ | SelectionKey.OP_WRITE, attachment);
        }
    }

    private void handleReadEvent(SelectableChannel channel, Object attachment) throws IOException {
        if (!(channel instanceof SocketChannel)) {
            return;
        }
        if (!(attachment instanceof ConnectionAttachment)) {
            return;
        }
        SocketChannel clientChannel = (SocketChannel) channel;
        ConnectionAttachment connectionAttachment = (ConnectionAttachment) attachment;

        ByteBuffer readBuffer = connectionAttachment.getReadBuffer();

        int bytesRead = clientChannel.read(readBuffer);

        if (bytesRead < 0) {
            closeClient(clientChannel, connectionAttachment);
            return;
        }

        if (bytesRead == 0)
            return;

        try {
            readBuffer.flip();
            connectionAttachment.getChannelPipeline().fireChannelRead(readBuffer);
        } finally {
            readBuffer.compact();
        }

    }

    private void handleWriteEvent(SelectableChannel channel, Object attachment) throws IOException {
        if (!(channel instanceof SocketChannel)) {
            return;
        }
        if (!(attachment instanceof ConnectionAttachment)) {
            return;
        }

        ConnectionAttachment connectionAttachment = (ConnectionAttachment) attachment;

        SocketChannel socketChannel = (SocketChannel) channel;

        Deque<ByteBuffer> outboundQueue = connectionAttachment.getOutboundQueue();

        while (!outboundQueue.isEmpty()) {

            ByteBuffer buffer = outboundQueue.peek();

            int written = socketChannel.write(buffer);

            if (written < 0) {
                closeClient(socketChannel, connectionAttachment);
            } else if (buffer.hasRemaining()) {
                break;
            }

            outboundQueue.poll();
        }

    }

    private void closeClient(SocketChannel clientChannel, ConnectionAttachment attachment) {
        NioSocketChannel nioChannel = attachment != null ? attachment.getNioSocketChannel() : null;
        if (nioChannel == null) {
            nioChannel = new NioSocketChannel(clientChannel);
        }
        try {
            sessionStore.removeByChannel(nioChannel);
        } catch (Exception ignored) {
            // Template stage: cleanup failures should not block channel close.
        }
        try {
            poller.deregister(clientChannel);
        } catch (Exception ignored) {
            // Best effort.
        }
        try {
            clientChannel.close();
        } catch (IOException ignored) {
            // Best effort.
        }
    }

    private void safeCloseEventChannel(PollerEvent event) {
        if (event == null || event.channel() == null) {
            return;
        }
        if (event.channel() instanceof SocketChannel) {
            ConnectionAttachment attachment = null;
            if (event.attachment() instanceof ConnectionAttachment) {
                attachment = (ConnectionAttachment) event.attachment();
            }
            closeClient((SocketChannel) event.channel(), attachment);
            return;
        }
        try {
            poller.deregister(event.channel());
            event.channel().close();
        } catch (Exception ignored) {
            // Best effort for template.
        }
    }

}
