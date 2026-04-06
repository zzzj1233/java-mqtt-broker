package com.playground.mqtt.router;

import com.playground.mqtt.protocol.frame.MqttFrame;
import com.playground.mqtt.session.ClientSession;

public final class DefaultMessageRouter implements MessageRouter {

    @Override
    public void routePublish(ClientSession source, MqttFrame frame) {
        // TODO: dispatch by topic to matched subscribers.
    }
}
