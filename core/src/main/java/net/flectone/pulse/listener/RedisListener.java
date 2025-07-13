package net.flectone.pulse.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.lettuce.core.pubsub.RedisPubSubListener;
import net.flectone.pulse.handler.ProxyMessageHandler;
import net.flectone.pulse.proxy.RedisProxy;

@Singleton
public class RedisListener implements RedisPubSubListener<byte[], byte[]> {

    private final RedisProxy redisProxySender;
    private final ProxyMessageHandler proxyMessageHandler;

    @Inject
    public RedisListener(RedisProxy redisProxySender,
                         ProxyMessageHandler proxyMessageHandler) {
        this.redisProxySender = redisProxySender;
        this.proxyMessageHandler = proxyMessageHandler;
    }

    @Override
    public void message(byte[] channel, byte[] message) {
        if (!redisProxySender.isEnable()) return;

        proxyMessageHandler.handleProxyMessage(message);
    }

    @Override
    public void message(byte[] bytes, byte[] k1, byte[] bytes2) {
    }

    @Override
    public void subscribed(byte[] bytes, long l) {
    }

    @Override
    public void psubscribed(byte[] bytes, long l) {
    }

    @Override
    public void unsubscribed(byte[] bytes, long l) {
    }

    @Override
    public void punsubscribed(byte[] bytes, long l) {
    }

}
