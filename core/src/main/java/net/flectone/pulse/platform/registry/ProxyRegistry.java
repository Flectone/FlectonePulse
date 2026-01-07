package net.flectone.pulse.platform.registry;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.platform.proxy.Proxy;
import net.flectone.pulse.platform.proxy.RedisProxy;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.util.logging.FLogger;

import java.util.ArrayList;
import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ProxyRegistry implements Registry {

    @Getter private final List<Proxy> proxies = new ArrayList<>();

    private final FileFacade fileFacade;
    private final ReflectionResolver reflectionResolver;
    private final FLogger fLogger;
    private final Injector injector;

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
