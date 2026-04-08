package com.playground.mqtt.protocol.frame;

public enum MqttPacketType {
    CONNECT(1),
    CONNACK(2),
    PUBLISH(3),
    PUBACK(4),
    SUBSCRIBE(8),
    SUBACK(9),
    UNSUBSCRIBE(10),
    UNSUBACK(11),
    PINGREQ(12),
    PINGRESP(13),
    DISCONNECT(14),
    UNKNOWN(-1);

    private final int typeNibble;

    MqttPacketType(int typeNibble) {
        this.typeNibble = typeNibble;
    }

    public int typeNibble() {
        return typeNibble;
    }

    public static MqttPacketType fromTypeNibble(int typeNibble) {
        for (MqttPacketType packetType : values()) {
            if (packetType.typeNibble == typeNibble) {
                return packetType;
            }
        }
        return UNKNOWN;
    }
}
