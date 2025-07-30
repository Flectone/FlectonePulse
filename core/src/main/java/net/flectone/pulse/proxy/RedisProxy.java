package net.flectone.pulse.proxy;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.listener.RedisListener;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.Proxy;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.resolver.SystemVariableResolver;
import net.flectone.pulse.constant.MessageType;
import net.flectone.pulse.util.logging.FLogger;

import java.nio.charset.StandardCharsets;

@Singleton
public class RedisProxy implements Proxy {

    private final Config.Redis config;
    private final Config.Database database;
    private final FLogger fLogger;
    private final Provider<RedisListener> redisListenerProvider;
    private final SystemVariableResolver systemVariableResolver;

    private RedisClient redisClient;
    private StatefulRedisPubSubConnection<byte[], byte[]> pubSubConnection;

    @Inject
    public RedisProxy(FileResolver fileResolver,
                      FLogger fLogger,
                      Provider<RedisListener> redisListenerProvider,
                      SystemVariableResolver systemVariableResolver) {
        this.config = fileResolver.getConfig().getRedis();
        this.database = fileResolver.getConfig().getDatabase();
        this.fLogger = fLogger;
        this.redisListenerProvider = redisListenerProvider;
        this.systemVariableResolver = systemVariableResolver;
    }

    @Override
    public boolean isEnable() {
        return config.isEnable() && database.getType() == Database.Type.MYSQL
                && pubSubConnection != null && pubSubConnection.isOpen();
    }

    @Override
    public void onEnable() {
        if (pubSubConnection != null) {
            pubSubConnection.close();
        }

        RedisURI.Builder uriBuilder = RedisURI.builder()
                .withHost(config.getHost())
                .withPort(config.getPort())
                .withSsl(config.isSsl());

        if (!config.getUser().isEmpty() && !config.getPassword().isEmpty()) {
            uriBuilder.withAuthentication(
                    systemVariableResolver.substituteEnvVars(config.getUser()),
                    systemVariableResolver.substituteEnvVars(config.getPassword())
            );
        }

        this.redisClient = RedisClient.create(uriBuilder.build());
        this.pubSubConnection = redisClient.connectPubSub(new ByteArrayCodec());

        try {
            pubSubConnection.sync().ping();
            fLogger.info("Redis (Lettuce) connected");

            RedisPubSubAsyncCommands<byte[], byte[]> async = pubSubConnection.async();
            for (MessageType tag : MessageType.values()) {
                async.subscribe(tag.name().getBytes(StandardCharsets.UTF_8));
            }

            pubSubConnection.addListener(redisListenerProvider.get());

        } catch (Exception e) {
            fLogger.warning("Redis connection failed: " + e.getMessage());
            onDisable();
        }
    }

    @Override
    public void onDisable() {
        if (pubSubConnection != null) {
            pubSubConnection.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
    }

    @Override
    public boolean sendMessage(FEntity sender, MessageType tag, byte[] message) {
        if (!isEnable()) return false;
        if (tag == null) return false;

        pubSubConnection.async().publish(
                tag.name().getBytes(StandardCharsets.UTF_8),
                message
        );

        return true;
    }
}