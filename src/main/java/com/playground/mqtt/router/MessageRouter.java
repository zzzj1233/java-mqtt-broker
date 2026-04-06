package com.playground.mqtt.router;

import com.playground.mqtt.protocol.frame.MqttFrame;
import com.playground.mqtt.session.ClientSession;

public interface MessageRouter {

    void routePublish(ClientSession source, MqttFrame frame);
}
