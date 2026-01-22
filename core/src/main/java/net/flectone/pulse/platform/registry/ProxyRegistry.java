package net.flectone.pulse.platform.registry;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.platform.proxy.Proxy;
import net.flectone.pulse.platform.proxy.RedisProxy;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ProxyRegistry {

    private final List<Proxy> proxies = new CopyOnWriteArrayList<>();

    private final FileFacade fileFacade;
    private final ReflectionResolver reflectionResolver;
    private final FLogger fLogger;
    private final Injector injector;

    public Collection<Proxy> getProxies() {
        return Collections.unmodifiableList(proxies);
    }

    public boolean hasEnabledProxy() {
        return proxies.stream().anyMatch(Proxy::isEnable);
    }

    public void registry(Proxy proxy) {
        proxies.add(proxy);
    }

    public void onEnable() {
        Config.Proxy.Redis redis = fileFacade.config().proxy().redis();
        if (redis.enable()) {
            warnIfLocalDatabase();

            reflectionResolver.hasClassOrElse("net.flectone.pulse.library.lettuce.core.RedisClient", this::loadLibraries);

            RedisProxy redisProxy = injector.getInstance(RedisProxy.class);
            redisProxy.onEnable();

            registry(redisProxy);
        }
    }

    protected void warnIfLocalDatabase() {
        Config.Database database = fileFacade.config().database();
        if (database.type() == Database.Type.SQLITE || database.type() == Database.Type.H2) {
            fLogger.warning("SQLITE/H2 database and Proxy are incompatible");
        }
    }

    public void onDisable() {
        proxies.forEach(Proxy::onDisable);
        proxies.clear();
    }

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
                .relocate(Relocation.builder()
                        .pattern("io{}lettuce")
                        .relocatedPattern("net.flectone.pulse.library.lettuce")
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("io{}netty")
                        .relocatedPattern("net.flectone.pulse.library.lettuce.netty")
                        .build()
                )
                .build()
        );
    }

}
