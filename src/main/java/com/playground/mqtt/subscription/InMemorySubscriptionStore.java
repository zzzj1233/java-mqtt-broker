package com.playground.mqtt.subscription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class InMemorySubscriptionStore implements SubscriptionStore {
    private static final Logger LOG = LoggerFactory.getLogger(InMemorySubscriptionStore.class);

    private final Map<String, List<Subscription>> clientIdToSubscription = new ConcurrentHashMap<>();

    @Override
    public void add(Subscription subscription) {

        clientIdToSubscription.computeIfAbsent(
                subscription.clientId(),
                s -> Collections.synchronizedList(new ArrayList<>())
        ).add(subscription);
        LOG.debug(
                "SubscriptionStore add clientId={} topic={} qos={} totalClients={}",
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
        LOG.debug(
                "SubscriptionStore match topic={} matched={}",
                topicName,
                matched.size()
        );
        return matched;

    }
}
