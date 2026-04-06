package com.playground.mqtt.protocol.decode;

import com.playground.mqtt.protocol.frame.MqttFrame;

public interface PacketDecoder {

    MqttFrame decode(DecodedPacket packet);
}
