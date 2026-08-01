package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.platform.proxy.BukkitProxy;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class BukkitProxyRegistry extends ProxyRegistry {

    private final FileFacade fileFacade;
    private final Injector injector;

    @Inject
    public BukkitProxyRegistry(FileFacade fileFacade,
                               ReflectionResolver reflectionResolver,
                               FLogger fLogger,
                               Injector injector) {
        super(fileFacade, reflectionResolver, fLogger, injector);

        this.fileFacade = fileFacade;
        this.injector = injector;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Config config = fileFacade.config();
        boolean isBukkitProxyEnable = config.proxy().bungeecord() || config.proxy().velocity();
        if (isBukkitProxyEnable) {
            warnIfLocalDatabase();

            BukkitProxy bukkitProxy = injector.getInstance(BukkitProxy.class);
            bukkitProxy.onEnable();

            registry(bukkitProxy);
        }
    }

}
