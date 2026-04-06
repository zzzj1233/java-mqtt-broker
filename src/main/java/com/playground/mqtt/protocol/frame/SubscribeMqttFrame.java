package com.playground.mqtt.protocol.frame;

import java.nio.ByteBuffer;

public class SubscribeMqttFrame extends MqttFrame {

    private final int packetId;
    private final String topicFilter;
    private final int requestedQos;

    public SubscribeMqttFrame(int packetId, String topicFilter, int requestedQos, ByteBuffer payload) {
        super(MqttPacketType.SUBSCRIBE, payload);
        this.packetId = packetId;
        this.topicFilter = topicFilter;
        this.requestedQos = requestedQos;
    }

    public String getTopicFilter() {
        return topicFilter;
    }

    public int getRequestedQos() {
        return requestedQos;
    }

    public int getPacketId() {
        return packetId;
    }
}
