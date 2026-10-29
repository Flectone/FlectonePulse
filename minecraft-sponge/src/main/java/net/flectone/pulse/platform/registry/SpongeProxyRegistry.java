package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.platform.proxy.RedisProxy;
import net.flectone.pulse.platform.proxy.SpongeProxy;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.flectone.pulse.util.LazyInstance;

@Singleton
public class SpongeProxyRegistry extends ProxyRegistryImpl {

    private final FileFacade fileFacade;
    private final LazyInstance<SpongeProxy> spongeProxy;

    @Inject
    public SpongeProxyRegistry(FileFacade fileFacade,
                               ReflectionResolver reflectionResolver,
                               FLogger fLogger,
                               LazyInstance<SpongeProxy> spongeProxy,
                               LazyInstance<RedisProxy> redisProxy) {
        super(fileFacade, reflectionResolver, fLogger, redisProxy);

        this.fileFacade = fileFacade;
        this.spongeProxy = spongeProxy;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Config config = fileFacade.config();
        if (config.proxy().bungeecord() || config.proxy().velocity()) {
            warnIfLocalDatabase();

            SpongeProxy proxy = spongeProxy.get();
            proxy.onEnable();

            registry(proxy);
        }
    }
}