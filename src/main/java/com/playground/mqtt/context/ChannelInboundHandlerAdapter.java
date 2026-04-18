package com.playground.mqtt.context;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Generic inbound adapter.
 *
 * <p>Only messages that match {@code inboundType} are handled by {@link #channelRead0}.
 * Unmatched messages are propagated to next inbound handler.
 */
public abstract class ChannelInboundHandlerAdapter<I> implements ChannelInboundHandler {

    private final Class<? extends I> inboundType;

    @SuppressWarnings("unchecked")
    protected ChannelInboundHandlerAdapter() {
        this.inboundType = (Class<? extends I>) inferInboundType(getClass());
    }

    protected ChannelInboundHandlerAdapter(Class<? extends I> inboundType) {
        this.inboundType = Objects.requireNonNull(inboundType, "inboundType");
    }

    @Override
    public final void channelRead(ChannelContext ctx, Object msg) {
        if (!inboundType.isInstance(msg)) {
            ctx.fireChannelRead(msg);
            return;
        }
        channelRead0(ctx, inboundType.cast(msg));
    }

    protected abstract void channelRead0(ChannelContext ctx, I msg);

    private static Class<?> inferInboundType(Class<?> implClass) {
        Class<?> cursor = implClass;
        while (cursor != null && cursor != Object.class) {
            Type genericSuper = cursor.getGenericSuperclass();
            if (genericSuper instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericSuper;
                Type rawType = pt.getRawType();
                if (rawType instanceof Class
                        && ChannelInboundHandlerAdapter.class.isAssignableFrom((Class<?>) rawType)) {
                    Type actual = pt.getActualTypeArguments()[0];
                    if (actual instanceof Class) {
                        return (Class<?>) actual;
                    }
                    if (actual instanceof ParameterizedType) {
                        Type nestedRawType = ((ParameterizedType) actual).getRawType();
                        if (nestedRawType instanceof Class) {
                            return (Class<?>) nestedRawType;
                        }
                    }
                }
            }
            cursor = cursor.getSuperclass();
        }
        throw new IllegalStateException(
                "Cannot infer inbound message type. Use ChannelInboundHandlerAdapter(Class<?>) constructor.");
    }
}
