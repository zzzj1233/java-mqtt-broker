package com.playground.mqtt.qos;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shared payload id with explicit reference count.
 */
public final class RefCountedPublishPayloadId {

    private final long id;
    private final AtomicInteger refCount = new AtomicInteger(0);

    public RefCountedPublishPayloadId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("payload id must be positive");
        }
        this.id = id;
    }

    public long value() {
        return id;
    }

    public int refCount() {
        return refCount.get();
    }

    public int retain() {
        return refCount.incrementAndGet();
    }

    public int release() {
        while (true) {
            int current = refCount.get();
            if (current <= 0) {
                throw new IllegalStateException("release without retain, payloadId=" + id);
            }
            if (refCount.compareAndSet(current, current - 1)) {
                return current - 1;
            }
        }
    }

    public int releaseSilent() {
        while (true) {
            int current = refCount.get();
            if (current <= 0) {
                return -1;
            }
            if (refCount.compareAndSet(current, current - 1)) {
                return current - 1;
            }
        }
    }
}
