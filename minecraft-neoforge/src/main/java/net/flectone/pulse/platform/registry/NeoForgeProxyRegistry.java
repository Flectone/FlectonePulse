package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.platform.proxy.NeoForgeProxy;
import net.flectone.pulse.platform.proxy.RedisProxy;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.flectone.pulse.util.LazyInstance;

@Singleton
public class NeoForgeProxyRegistry extends ProxyRegistryImpl {

    private final FileFacade fileFacade;
    private final LazyInstance<NeoForgeProxy> neoForgeProxy;

    @Inject
    public NeoForgeProxyRegistry(FileFacade fileFacade,
                                 ReflectionResolver reflectionResolver,
                                 FLogger fLogger,
                                 LazyInstance<NeoForgeProxy> neoForgeProxy,
                                 LazyInstance<RedisProxy> redisProxy) {
        super(fileFacade, reflectionResolver, fLogger, redisProxy);

        this.fileFacade = fileFacade;
        this.neoForgeProxy = neoForgeProxy;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Config config = fileFacade.config();
        if (config.proxy().bungeecord() || config.proxy().velocity()) {
            warnIfLocalDatabase();

            NeoForgeProxy proxy = neoForgeProxy.get();
            proxy.onEnable();

            registry(proxy);
        }
    }
}