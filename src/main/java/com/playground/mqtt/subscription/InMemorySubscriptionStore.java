package com.playground.mqtt.subscription;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class InMemorySubscriptionStore implements SubscriptionStore {

    private final Map<String, List<Subscription>> clientIdToSubscription = new ConcurrentHashMap<>();

    @Override
    public void add(Subscription subscription) {

        clientIdToSubscription.computeIfAbsent(
                subscription.clientId(),
                s -> Collections.synchronizedList(new ArrayList<>())
        ).add(subscription);
    }

    @Override
    public void removeByClientId(String clientId) {

        clientIdToSubscription.remove(clientId);
    }

    @Override
    public List<Subscription> matchTopic(String topicName) {

        return clientIdToSubscription.values().stream()
                .flatMap(Collection::stream)
                .filter(subscription -> subscription.topicFilter().equals(topicName))
                .collect(Collectors.toList());

    }
}
