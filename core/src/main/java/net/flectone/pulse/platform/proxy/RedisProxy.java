package net.flectone.pulse.platform.proxy;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.listener.RedisListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.logging.FLogger;

import java.nio.charset.StandardCharsets;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RedisProxy implements Proxy {

    private final FileFacade fileFacade;
    private final FLogger fLogger;
    private final Provider<RedisListener> redisListenerProvider;
    private final SystemVariableResolver systemVariableResolver;

    private RedisClient redisClient;
    private StatefulRedisPubSubConnection<byte[], byte[]> pubSubConnection;

    public Config.Proxy.Redis config() {
        return fileFacade.config().proxy().redis();
    }

    @Override
    public boolean isEnable() {
        Database.Type database = fileFacade.config().database().type();
        boolean serverDatabase = database == Database.Type.MYSQL
                || database == Database.Type.MARIADB
                || database == Database.Type.POSTGRESQL;

        return config().enable() && serverDatabase && pubSubConnection != null && pubSubConnection.isOpen();
    }

    @Override
    public void onEnable() {
        if (pubSubConnection != null) {
            pubSubConnection.close();
        }

        RedisURI.Builder uriBuilder = RedisURI.builder()
                .withHost(config().host())
                .withPort(config().port())
                .withSsl(config().ssl());

        if (!config().user().isEmpty() && !config().password().isEmpty()) {
            uriBuilder.withAuthentication(
                    systemVariableResolver.substituteEnvVars(config().user()),
                    systemVariableResolver.substituteEnvVars(config().password())
            );
        }

        this.redisClient = RedisClient.create(uriBuilder.build());
        this.pubSubConnection = redisClient.connectPubSub(new ByteArrayCodec());

        try {
            RedisPubSubAsyncCommands<byte[], byte[]> async = pubSubConnection.async();
            for (MessageType tag : MessageType.values()) {
                async.subscribe(tag.name().getBytes(StandardCharsets.UTF_8));
            }

            pubSubConnection.addListener(redisListenerProvider.get());

            fLogger.info("Redis (Lettuce) connected");
        } catch (Exception e) {
            fLogger.warning("Redis connection failed: ", e);
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