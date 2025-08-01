package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.platform.proxy.BukkitProxy;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class BukkitProxyRegistry extends ProxyRegistry {

    private final FileResolver fileResolver;
    private final Injector injector;

    @Inject
    public BukkitProxyRegistry(FileResolver fileResolver,
                               LibraryResolver libraryResolver,
                               FLogger fLogger,
                               Injector injector) {
        super(fileResolver, libraryResolver, fLogger, injector);

        this.fileResolver = fileResolver;
        this.injector = injector;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Config config = fileResolver.getConfig();
        boolean isBukkitProxyEnable = config.isBungeecord() || config.isVelocity();
        if (isBukkitProxyEnable) {
            warnIfLocalDatabase();

            BukkitProxy bukkitProxy = injector.getInstance(BukkitProxy.class);
            bukkitProxy.onEnable();

            registry(bukkitProxy);
        }
    }

}
