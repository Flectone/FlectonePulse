package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.platform.proxy.BukkitProxy;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class BukkitProxyRegistry extends ProxyRegistry {

    private final FileResolver fileResolver;
    private final Injector injector;

    @Inject
    public BukkitProxyRegistry(FileResolver fileResolver,
                               ReflectionResolver reflectionResolver,
                               FLogger fLogger,
                               Injector injector) {
        super(fileResolver, reflectionResolver, fLogger, injector);

        this.fileResolver = fileResolver;
        this.injector = injector;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Config config = fileResolver.getConfig();
        boolean isBukkitProxyEnable = config.getProxy().isBungeecord() || config.getProxy().isVelocity();
        if (isBukkitProxyEnable) {
            warnIfLocalDatabase();

            BukkitProxy bukkitProxy = injector.getInstance(BukkitProxy.class);
            bukkitProxy.onEnable();

            registry(bukkitProxy);
        }
    }

}
