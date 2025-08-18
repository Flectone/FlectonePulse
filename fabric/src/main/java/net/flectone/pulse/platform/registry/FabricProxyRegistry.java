package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.platform.proxy.FabricProxy;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class FabricProxyRegistry extends ProxyRegistry {

    private final FileResolver fileResolver;
    private final Injector injector;
    private final FLogger fLogger;

    @Inject
    public FabricProxyRegistry(FileResolver fileResolver,
                               ReflectionResolver reflectionResolver,
                               FLogger fLogger,
                               Injector injector) {
        super(fileResolver, reflectionResolver, fLogger, injector);

        this.fileResolver = fileResolver;
        this.injector = injector;
        this.fLogger = fLogger;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Config config = fileResolver.getConfig();
        if (config.isBungeecord()) {
            fLogger.warning("BungeeCord is not supported on Fabric");
            return;
        }

        if (config.isVelocity()) {
            warnIfLocalDatabase();

            FabricProxy fabricProxy = injector.getInstance(FabricProxy.class);
            fabricProxy.onEnable();

            registry(fabricProxy);
        }
    }

}
