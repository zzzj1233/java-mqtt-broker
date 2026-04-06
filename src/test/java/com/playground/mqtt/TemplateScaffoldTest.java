package com.playground.mqtt;

import com.playground.mqtt.config.BrokerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateScaffoldTest {

    @Test
    void shouldLoadDefaultConfigForTemplate() {
        BrokerConfig config = BrokerConfig.fromEnv();
        assertTrue(config.port() > 0);
        assertTrue(config.backlog() > 0);
    }
}
