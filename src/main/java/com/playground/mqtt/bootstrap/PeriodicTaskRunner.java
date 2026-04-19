package com.playground.mqtt.bootstrap;

/**
 * Periodic task callback invoked by broker event loop.
 */
public interface PeriodicTaskRunner {

    void runOnce();
}
