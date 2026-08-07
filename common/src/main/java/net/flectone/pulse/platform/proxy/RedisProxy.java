package net.flectone.pulse.platform.proxy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.constant.DatabaseType;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.listener.proxy.RedisProxyListener;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.resolver.SystemVariableResolver;
import net.flectone.pulse.util.LazyInstance;
import org.jspecify.annotations.NonNull;

import java.nio.charset.StandardCharsets;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RedisProxy implements Proxy {

    private final FileFacade fileFacade;
    private final FLogger fLogger;
    private final LazyInstance<RedisProxyListener> redisListener;
    private final SystemVariableResolver systemVariableResolver;

    private RedisClient redisClient;
    private StatefulRedisPubSubConnection<byte[], byte[]> pubSubConnection;

    public Config.Proxy.Redis config() {
        return fileFacade.config().proxy().redis();
    }

    @Override
    public boolean isEnable() {
        DatabaseType database = fileFacade.config().database().type();
        boolean serverDatabase = database == DatabaseType.MYSQL
                || database == DatabaseType.MARIADB
                || database == DatabaseType.POSTGRESQL;

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
            for (ModuleName tag : ModuleName.values()) {
                async.subscribe(tag.name().getBytes(StandardCharsets.UTF_8));
            }

            pubSubConnection.addListener(redisListener.get());

            fLogger.info("Redis (Lettuce) connected");
        } catch (Exception e) {
            fLogger.warning(e, "Redis connection failed");
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
    public boolean sendMessage(@NonNull FEntity sender, @NonNull ModuleName tag, byte @NonNull [] message) {
        if (!isEnable()) return false;

        pubSubConnection.async().publish(
                tag.name().getBytes(StandardCharsets.UTF_8),
                message
        );

        return true;
    }
}