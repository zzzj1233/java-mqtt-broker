package com.playground.mqtt.protocol.frame;

import java.nio.ByteBuffer;

public class PublishMqttFrame extends MqttFrame {

    private final int dup;
    private final int qos;
    private final String topic;
    private final Integer packetId;

    public PublishMqttFrame(
            int dup,
            int qos,
            String topic,
            Integer packetId,
            ByteBuffer payload) {
        super(MqttPacketType.PUBLISH, payload);

        this.dup = dup;
        this.qos = qos;
        this.topic = topic;
        this.packetId = packetId;
    }

    public int getDup() {
        return dup;
    }

    public int getQos() {
        return qos;
    }

    public String getTopic() {
        return topic;
    }

    public Integer getPacketId() {
        return packetId;
    }
}
