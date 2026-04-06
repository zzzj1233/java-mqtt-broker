package com.playground.mqtt.protocol.encode;

import com.playground.mqtt.protocol.frame.MqttFrame;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface PacketEncoder {

    ByteBuffer encode(MqttFrame frame) throws IOException;

}
