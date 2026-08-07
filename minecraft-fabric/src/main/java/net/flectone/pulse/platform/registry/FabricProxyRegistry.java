package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.platform.proxy.FabricProxy;
import net.flectone.pulse.platform.proxy.RedisProxy;
import net.flectone.pulse.platform.regitry.ProxyRegistryImpl;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.LazyInstance;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class FabricProxyRegistry extends ProxyRegistryImpl {

    private final FileFacade fileFacade;
    private final LazyInstance<FabricProxy> fabricProxy;

    @Inject
    public FabricProxyRegistry(FileFacade fileFacade,
                               ReflectionResolver reflectionResolver,
                               FLogger fLogger,
                               LazyInstance<FabricProxy> fabricProxy,
                               LazyInstance<RedisProxy> redisProxy) {
        super(fileFacade, reflectionResolver, fLogger, redisProxy);

        this.fileFacade = fileFacade;
        this.fabricProxy = fabricProxy;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Config config = fileFacade.config();
        if (config.proxy().bungeecord() || config.proxy().velocity()) {
            warnIfLocalDatabase();

            FabricProxy proxy = fabricProxy.get();
            proxy.onEnable();

            registry(proxy);
        }
    }

}