package com.playground.mqtt.protocol.frame;

import java.nio.ByteBuffer;

public final class ConnectMqttFrame extends MqttFrame {

    private final String protocolName;
    private final int protocolLevel;
    private final int connectFlags;
    private final int keepAliveSeconds;
    private final String clientId;

    public ConnectMqttFrame(
            ByteBuffer payload,
            String protocolName,
            int protocolLevel,
            int connectFlags,
            int keepAliveSeconds,
            String clientId
    ) {
        super(MqttPacketType.CONNECT, payload);
        this.protocolName = protocolName;
        this.protocolLevel = protocolLevel;
        this.connectFlags = connectFlags;
        this.keepAliveSeconds = keepAliveSeconds;
        this.clientId = clientId;
    }

    public String protocolName() {
        return protocolName;
    }

    public int protocolLevel() {
        return protocolLevel;
    }

    public int connectFlags() {
        return connectFlags;
    }

    public int keepAliveSeconds() {
        return keepAliveSeconds;
    }

    public String clientId() {
        return clientId;
    }

    public boolean cleanSession() {
        return (connectFlags & 0x02) != 0;
    }

    public boolean willFlag() {
        return (connectFlags & 0x04) != 0;
    }

    public int willQos() {
        return (connectFlags >>> 3) & 0x03;
    }

    public boolean willRetain() {
        return (connectFlags & 0x20) != 0;
    }

    public boolean passwordFlag() {
        return (connectFlags & 0x40) != 0;
    }

    public boolean usernameFlag() {
        return (connectFlags & 0x80) != 0;
    }

    @Override
    public String toString() {
        return "ConnectMqttFrame{protocolName=" + protocolName
                + ", protocolLevel=" + protocolLevel
                + ", keepAliveSeconds=" + keepAliveSeconds
                + ", cleanSession=" + cleanSession()
                + ", clientId=" + clientId
                + ", payloadRemaining=" + (payload() == null ? "null" : payload().remaining())
                + "}";
    }
}
