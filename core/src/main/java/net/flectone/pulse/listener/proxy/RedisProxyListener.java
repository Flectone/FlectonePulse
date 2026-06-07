package net.flectone.pulse.listener.proxy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.lettuce.core.pubsub.RedisPubSubListener;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.listener.message.ProxyMessageListener;
import net.flectone.pulse.platform.proxy.RedisProxy;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RedisProxyListener implements RedisPubSubListener<byte[], byte[]> {

    private final RedisProxy redisProxySender;
    private final ProxyMessageListener proxyMessageListener;

    @Override
    public void message(byte[] channel, byte[] message) {
        if (!redisProxySender.isEnable()) return;

        proxyMessageListener.handleProxyMessage(message);
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
