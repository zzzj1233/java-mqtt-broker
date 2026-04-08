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
        System.out.printf(
                "SubscriptionStore add clientId=%s topic=%s qos=%d totalClients=%d%n",
                subscription.clientId(),
                subscription.topicFilter(),
                subscription.qos(),
                clientIdToSubscription.size()
        );
    }

    @Override
    public void removeByClientId(String clientId) {

        clientIdToSubscription.remove(clientId);
    }

    @Override
    public List<Subscription> matchTopic(String topicName) {

        List<Subscription> matched = clientIdToSubscription.values().stream()
                .flatMap(Collection::stream)
                .filter(subscription -> subscription.topicFilter().equals(topicName))
                .collect(Collectors.toList());
        System.out.printf(
                "SubscriptionStore match topic=%s matched=%d%n",
                topicName,
                matched.size()
        );
        return matched;

    }
}
