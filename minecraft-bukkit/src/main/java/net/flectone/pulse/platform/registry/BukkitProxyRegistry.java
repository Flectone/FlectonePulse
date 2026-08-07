package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.platform.proxy.BukkitProxy;
import net.flectone.pulse.platform.proxy.RedisProxy;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.flectone.pulse.util.LazyInstance;

@Singleton
public class BukkitProxyRegistry extends ProxyRegistryImpl {

    private final FileFacade fileFacade;
    private final LazyInstance<BukkitProxy> bukkitProxy;

    @Inject
    public BukkitProxyRegistry(FileFacade fileFacade,
                               ReflectionResolver reflectionResolver,
                               FLogger fLogger,
                               LazyInstance<RedisProxy> redisProxy,
                               LazyInstance<BukkitProxy> bukkitProxy) {
        super(fileFacade, reflectionResolver, fLogger, redisProxy);

        this.fileFacade = fileFacade;
        this.bukkitProxy = bukkitProxy;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Config config = fileFacade.config();
        boolean isBukkitProxyEnable = config.proxy().bungeecord() || config.proxy().velocity();
        if (isBukkitProxyEnable) {
            warnIfLocalDatabase();

            BukkitProxy proxy = bukkitProxy.get();
            proxy.onEnable();

            registry(proxy);
        }
    }

}