package com.playground.mqtt.transport.poller;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.List;

public interface Poller extends AutoCloseable {

    void register(SelectableChannel channel, int interestOps, Object attachment) throws IOException;

    void updateInterestOps(SelectableChannel channel, int interestOps) throws IOException;

    void deregister(SelectableChannel channel) throws IOException;

    List<PollerEvent> poll(int timeoutMs) throws IOException;

    @Override
    void close() throws IOException;
}
