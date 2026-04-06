package com.playground.mqtt.config;

public final class BrokerConfig {

    private final int port;
    private final int idleSeconds;
    private final int pollTimeoutMs;
    private final int maxConnections;
    private final int backlog;

    public BrokerConfig(int port, int idleSeconds, int pollTimeoutMs, int maxConnections, int backlog) {
        this.port = port;
        this.idleSeconds = idleSeconds;
        this.pollTimeoutMs = pollTimeoutMs;
        this.maxConnections = maxConnections;
        this.backlog = backlog;
    }

    public int port() {
        return port;
    }

    public int idleSeconds() {
        return idleSeconds;
    }

    public int pollTimeoutMs() {
        return pollTimeoutMs;
    }

    public int maxConnections() {
        return maxConnections;
    }

    public int backlog() {
        return backlog;
    }

    public static BrokerConfig fromEnv() {
        int port = parseInt("MQTT_PORT", 1883);
        int idleSeconds = parseInt("MQTT_IDLE_SECONDS", 120);
        int pollTimeoutMs = parseInt("MQTT_POLL_TIMEOUT_MS", 1000);
        int maxConnections = parseInt("MQTT_MAX_CONNECTIONS", 8192);
        int backlog = parseInt("MQTT_BACKLOG", 1024);
        return new BrokerConfig(port, idleSeconds, pollTimeoutMs, maxConnections, backlog);
    }

    private static int parseInt(String envName, int defaultValue) {
        String raw = System.getenv(envName);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }
}
