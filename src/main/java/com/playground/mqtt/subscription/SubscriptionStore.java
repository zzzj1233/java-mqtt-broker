package com.playground.mqtt.subscription;

import java.io.IOException;
import java.util.List;

public interface SubscriptionStore extends AutoCloseable {

    void add(Subscription subscription);

    void removeByClientId(String clientId);

    List<Subscription> matchTopic(String topicName);

    @Override
    default void close() throws IOException {
        // No-op by default.
    }
}
