package com.playground.mqtt.qos;

import com.playground.mqtt.protocol.frame.PublishMqttFrame;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PublishStore {

    private final AtomicLong outboundPublishPayloadIdGenerator = new AtomicLong(1L);

    private final Map<Long, PublishMqttFrame> store = new ConcurrentHashMap<>();

    public RefCountedPublishPayloadId store(PublishMqttFrame frame) {
        long id = this.nextOutboundPublishPayloadId();
        store.put(id, frame);
        return new RefCountedPublishPayloadId(id);
    }

    public PublishMqttFrame get(RefCountedPublishPayloadId payloadId) {
        return store.get(payloadId.value());
    }

    public void retain(RefCountedPublishPayloadId payloadId) {
        payloadId.retain();
    }

    public void release(RefCountedPublishPayloadId payloadId) {
        int remaining = payloadId.release();
        if (remaining == 0) {
            store.remove(payloadId.value());
        }
    }

    public void releaseSilent(RefCountedPublishPayloadId payloadId) {
        int remaining = payloadId.releaseSilent();
        if (remaining == 0) {
            store.remove(payloadId.value());
        }
    }

    public PublishMqttFrame remove(RefCountedPublishPayloadId payloadId) {
        return store.remove(payloadId.value());
    }

    private long nextOutboundPublishPayloadId() {
        long next = outboundPublishPayloadIdGenerator.getAndIncrement();
        if (next > 0) {
            return next;
        }
        outboundPublishPayloadIdGenerator.set(2L);
        return 1L;
    }

}
