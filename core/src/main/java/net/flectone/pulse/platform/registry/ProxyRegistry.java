package net.flectone.pulse.platform.registry;

import com.alessiodp.libby.Library;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.platform.proxy.Proxy;
import net.flectone.pulse.platform.proxy.RedisProxy;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.util.logging.FLogger;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class ProxyRegistry implements Registry {

    @Getter private final List<Proxy> proxies = new ArrayList<>();

    private final FileResolver fileResolver;
    private final ReflectionResolver reflectionResolver;
    private final FLogger fLogger;
    private final Injector injector;

    @Inject
    public ProxyRegistry(FileResolver fileResolver,
                         ReflectionResolver reflectionResolver,
                         FLogger fLogger,
                         Injector injector) {
        this.fileResolver = fileResolver;
        this.reflectionResolver = reflectionResolver;
        this.fLogger = fLogger;
        this.injector = injector;
    }

    public boolean hasEnabledProxy() {
        return proxies.stream().anyMatch(Proxy::isEnable);
    }

    public void registry(Proxy proxy) {
        proxies.add(proxy);
    }

    public void onEnable() {
        Config.Proxy.Redis redis = fileResolver.getConfig().getProxy().getRedis();
        if (redis.isEnable()) {
            warnIfLocalDatabase();

            reflectionResolver.hasClassOrElse("io.lettuce.core.RedisClient", this::loadLibraries);

            RedisProxy redisProxy = injector.getInstance(RedisProxy.class);
            redisProxy.onEnable();

            registry(redisProxy);
        }
    }

    protected void warnIfLocalDatabase() {
        Config.Database database = fileResolver.getConfig().getDatabase();
        if (database.getType() == Database.Type.SQLITE) {
            fLogger.warning("SQLITE database and Proxy are incompatible");
        }
    }

    public void onDisable() {
        proxies.forEach(Proxy::onDisable);
        proxies.clear();
    }

    @Override
    public void reload() {
        onDisable();
        onEnable();
    }

    private void loadLibraries(LibraryResolver libraryResolver) {
        libraryResolver.loadLibrary(Library.builder()
                .groupId("io{}lettuce")
                .artifactId("lettuce-core")
                .version(BuildConfig.LETTUCE_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );
    }

}
