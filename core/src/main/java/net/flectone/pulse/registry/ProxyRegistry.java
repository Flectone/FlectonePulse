package net.flectone.pulse.registry;

import com.alessiodp.libby.Library;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.proxy.Proxy;
import net.flectone.pulse.proxy.RedisProxy;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.util.logging.FLogger;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class ProxyRegistry implements Registry {

    @Getter private final List<Proxy> proxies = new ArrayList<>();

    private final FileResolver fileResolver;
    private final LibraryResolver libraryResolver;
    private final FLogger fLogger;
    private final Injector injector;

    @Inject
    public ProxyRegistry(FileResolver fileResolver,
                         LibraryResolver libraryResolver,
                         FLogger fLogger,
                         Injector injector) {
        this.fileResolver = fileResolver;
        this.libraryResolver = libraryResolver;
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
        Config.Redis redis = fileResolver.getConfig().getRedis();
        if (redis.isEnable()) {
            warnIfLocalDatabase();

            try {
                Class.forName("io.lettuce.core.RedisClient");
            } catch (ClassNotFoundException e) {
                loadLibraries();
            }

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

    private void loadLibraries() {
        libraryResolver.loadLibrary(Library.builder()
                .groupId("io{}lettuce")
                .artifactId("lettuce-core")
                .version(BuildConfig.LETTUCE_VERSION)
                .resolveTransitiveDependencies(true)
                .build()
        );
    }

}
