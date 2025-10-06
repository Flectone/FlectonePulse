package net.flectone.pulse.platform.proxy;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.listener.RedisListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.logging.FLogger;

import java.nio.charset.StandardCharsets;

@Singleton
public class RedisProxy implements Proxy {

    private final FileResolver fileResolver;
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
        this.fileResolver = fileResolver;
        this.fLogger = fLogger;
        this.redisListenerProvider = redisListenerProvider;
        this.systemVariableResolver = systemVariableResolver;
    }

    public Config.Proxy.Redis config() {
        return fileResolver.getConfig().getProxy().getRedis();
    }

    @Override
    public boolean isEnable() {
        Database.Type database = fileResolver.getConfig().getDatabase().getType();
        boolean serverDatabase = database == Database.Type.MYSQL
                || database == Database.Type.MARIADB
                || database == Database.Type.POSTGRESQL;

        return config().isEnable() && serverDatabase && pubSubConnection != null && pubSubConnection.isOpen();
    }

    @Override
    public void onEnable() {
        if (pubSubConnection != null) {
            pubSubConnection.close();
        }

        RedisURI.Builder uriBuilder = RedisURI.builder()
                .withHost(config().getHost())
                .withPort(config().getPort())
                .withSsl(config().isSsl());

        if (!config().getUser().isEmpty() && !config().getPassword().isEmpty()) {
            uriBuilder.withAuthentication(
                    systemVariableResolver.substituteEnvVars(config().getUser()),
                    systemVariableResolver.substituteEnvVars(config().getPassword())
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