package com.playground.mqtt.bootstrap;

import com.playground.mqtt.context.ChannelPipeline;
import com.playground.mqtt.protocol.frame.PublishMqttFrame;
import com.playground.mqtt.qos.*;
import com.playground.mqtt.session.ClientSession;
import com.playground.mqtt.session.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static com.playground.mqtt.qos.Qos1PacketState.IN_FLIGHT;

/**
 * Default no-op periodic task runner.
 *
 * <p>Use this class as the central place to evolve keepalive checks,
 * inflight retry scans and stale session cleanup over time.
 */
public final class DefaultPeriodicTaskRunner implements PeriodicTaskRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPeriodicTaskRunner.class);

    private final long retryIntervalNanos;
    private final int maxRetryCount;

    private final InMemoryQos1Store qos1Store;

    private final SessionStore sessionStore;

    private final PublishStore publishStore;

    private final InMemoryPacketIdGenerator packetIdGenerator;

    public DefaultPeriodicTaskRunner(
            InMemoryQos1Store qos1Store,
            SessionStore sessionStore,
            PublishStore publishStore,
            InMemoryPacketIdGenerator packetIdGenerator,
            int retryIntervalSeconds,
            int maxRetryCount
    ) {
        this.qos1Store = qos1Store;
        this.sessionStore = sessionStore;
        this.publishStore = publishStore;
        this.packetIdGenerator = packetIdGenerator;
        this.retryIntervalNanos = TimeUnit.SECONDS.toNanos(Math.max(1, retryIntervalSeconds));
        this.maxRetryCount = Math.max(1, maxRetryCount);
    }

    @Override
    public void runOnce() {

        ConcurrentMap<String, Map<Integer, Qos1InflightRecord>> outboundStates = qos1Store.getOutboundStates();

        for (Map.Entry<String, Map<Integer, Qos1InflightRecord>> entry : outboundStates.entrySet()) {

            String clientId = entry.getKey();

            Optional<ClientSession> session = sessionStore.findByClientId(clientId);

            if (session.isEmpty()) {
                // Client offline: release all shared payload refs before cleaning inflight states.
                Map<Integer, Qos1InflightRecord> removed = outboundStates.remove(clientId);
                if (removed != null) {

                    for (Map.Entry<Integer, Qos1InflightRecord> recordEntry : removed.entrySet()) {

                        Integer packetId = recordEntry.getKey();

                        Qos1InflightRecord record = recordEntry.getValue();

                        publishStore.release(record.publishPayloadId());

                        packetIdGenerator.cleanPacketId(clientId, packetId);
                    }

                }
                continue;
            }

            ChannelPipeline channelPipeline = session.get().channel().attachment().getChannelPipeline();

            Map<Integer, Qos1InflightRecord> recordMap = entry.getValue();

            for (Map.Entry<Integer, Qos1InflightRecord> recordEntry : recordMap.entrySet()) {

                Integer packetId = recordEntry.getKey();

                Qos1InflightRecord record = recordEntry.getValue();

                if (record.state() == IN_FLIGHT) {

                    long nowNanos = System.nanoTime();

                    if (nowNanos - record.lastSentAtNanos() < retryIntervalNanos) {
                        continue;
                    }

                    PublishMqttFrame frame;

                    if (record.retryCount() + 1 >= maxRetryCount || (frame = publishStore.get(record.publishPayloadId())) == null) {
                        if (recordMap.remove(packetId, record)) {
                            publishStore.releaseSilent(record.publishPayloadId());
                            packetIdGenerator.cleanPacketId(clientId, packetId);
                        }
                        continue;
                    }


                    PublishMqttFrame outboundFrame = new PublishMqttFrame(
                            1,
                            1,
                            frame.getTopic(),
                            packetId,
                            frame.payload().duplicate()
                    );

                    channelPipeline.fireChannelWrite(outboundFrame);

                    // 更新重试次数
                    Qos1InflightRecord retried = record.retried(nowNanos);

                    recordMap.compute(packetId, (ignored, old) -> old == record ? retried : old);
                }

            }
        }
    }

}
