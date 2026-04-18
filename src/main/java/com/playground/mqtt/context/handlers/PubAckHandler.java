package com.playground.mqtt.context.handlers;

import com.playground.mqtt.context.ChannelContext;
import com.playground.mqtt.context.ChannelInboundHandlerAdapter;
import com.playground.mqtt.protocol.frame.PubAckMqttFrame;
import com.playground.mqtt.qos.InMemoryQos1Store;
import com.playground.mqtt.qos.Qos1PacketState;
import com.playground.mqtt.session.ClientSession;
import com.playground.mqtt.session.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class PubAckHandler extends ChannelInboundHandlerAdapter<PubAckMqttFrame> {
    private static final Logger LOG = LoggerFactory.getLogger(PubAckHandler.class);

    private final InMemoryQos1Store qos1Store;

    private final SessionStore sessionStore;

    public PubAckHandler(InMemoryQos1Store qos1Store, SessionStore sessionStore) {
        this.qos1Store = qos1Store;
        this.sessionStore = sessionStore;
    }

    @Override
    protected void channelRead0(ChannelContext ctx, PubAckMqttFrame msg) {

        Optional<ClientSession> clientSession = sessionStore.findByChannel(ctx.nioChannel());

        if (clientSession.isEmpty()) {
            ctx.close();
            return;
        }

        String clientId = clientSession.get().clientId();
        int packetId = msg.getPacketId();
        LOG.info("PubAckHandler received PUBACK clientId={} packetId={}", clientId, packetId);

        if (qos1Store.updateOutboundState(clientId, packetId, Qos1PacketState.ACKED)) {
            boolean cleaned = qos1Store.cleanStatus(clientId, packetId, Qos1PacketState.ACKED);
            LOG.info("PubAckHandler outbound ACK state updated and cleaned clientId={} packetId={} cleaned={}",
                    clientId, packetId, cleaned);
        } else {
            LOG.debug("PubAckHandler ignored PUBACK without matching IN_FLIGHT state clientId={} packetId={}",
                    clientId, packetId);
        }
    }

}
