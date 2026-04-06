package com.playground.mqtt.subscription;

import java.util.List;

public final class InMemorySubscriptionStore implements SubscriptionStore {

    @Override
    public void add(Subscription subscription) {
        // TODO: maintain topic filter index and client index.
    }

    @Override
    public void removeByClientId(String clientId) {
        // TODO: remove all subscriptions from the client.
    }

    @Override
    public List<Subscription> matchTopic(String topicName) {
        return List.of();
    }
}
