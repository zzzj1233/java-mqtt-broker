package com.playground.mqtt.protocol.encode;

import com.playground.mqtt.protocol.frame.MqttFrame;
import com.playground.mqtt.protocol.frame.PublishMqttFrame;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PublishEncoder implements PacketEncoder {

    @Override
    public ByteBuffer encode(MqttFrame frame) throws IOException {

        PublishMqttFrame publishFrame = (PublishMqttFrame) frame;

        return null;
    }

    private int totalLength(
            PublishMqttFrame publishFrame
    ){
        // 1: type(4:high) + flags(4:low)
        // encodedRemainingLengthBytes: 1~4
        // remainingLength

        int remainingLength = remainingLength(publishFrame);
        return 1 + 1 + remainingLength;
    }

    private int remainingLength(PublishMqttFrame publishFrame) {
        // 2 + topic.length + qos > 0 ? 2 : 0 +  payloadLength
        return 2 + publishFrame.getTopic().length() + (publishFrame.getQos() > 0 ? 2 : 0) + publishFrame.payload().capacity();
    }

}
