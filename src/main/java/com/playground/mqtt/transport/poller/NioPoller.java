package com.playground.mqtt.transport.poller;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class NioPoller implements Poller {

    private final Selector selector;

    public NioPoller() throws IOException {
        this.selector = Selector.open();
    }

    @Override
    public void register(SelectableChannel channel, int interestOps, Object attachment) throws IOException {
        if (channel == null) {
            throw new IOException("channel is null");
        }
        try {
            channel.configureBlocking(false);
            channel.register(selector, interestOps, attachment);
            selector.wakeup();
        } catch (IOException e) {
            throw new IOException("Failed to register channel to selector", e);
        }
    }

    @Override
    public void updateInterestOps(SelectableChannel channel, int interestOps) throws IOException {
        if (channel == null) {
            throw new IOException("channel is null");
        }
        SelectionKey key = channel.keyFor(selector);
        if (key == null || !key.isValid()) {
            throw new IOException("SelectionKey is missing or invalid");
        }
        try {
            key.interestOps(interestOps);
            selector.wakeup();
        } catch (CancelledKeyException e) {
            throw new IOException("Failed to update interestOps for cancelled key", e);
        }
    }

    @Override
    public void deregister(SelectableChannel channel) throws IOException {
        if (channel == null) {
            return;
        }
        SelectionKey key = channel.keyFor(selector);
        if (key != null) {
            key.cancel();
            selector.wakeup();
        }
    }

    @Override
    public List<PollerEvent> poll(int timeoutMs) throws IOException {
        if (timeoutMs < 0) {
            throw new IOException("timeoutMs must be >= 0");
        }

        try {
            int selected = selector.select(timeoutMs);
            if (selected == 0) {
                return List.of();
            }

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            List<PollerEvent> events = new ArrayList<>(selected);
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (!key.isValid()) {
                    continue;
                }

                try {
                    PollerEvent pollerEvent = new PollerEvent(
                            key.channel(),
                            key.readyOps(),
                            key.attachment()
                    );
                    events.add(pollerEvent);
                } catch (CancelledKeyException ignored) {
                    // Per-channel race: key becomes invalid during iteration.
                }
            }
            return events;
        } catch (ClosedSelectorException e) {
            throw new IOException("Selector is closed", e);
        }
    }

    @Override
    public void close() throws IOException {
        selector.close();
    }
}
