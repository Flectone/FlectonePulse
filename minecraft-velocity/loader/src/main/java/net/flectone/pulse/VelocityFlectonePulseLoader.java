package net.flectone.pulse;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import lombok.Getter;

import java.util.function.Supplier;

@Plugin(
        id = "flectonepulse",
        name = "FlectonePulseVelocity",
        version = BuildConfig.PROJECT_VERSION,
        authors = BuildConfig.PROJECT_AUTHOR,
        description = BuildConfig.PROJECT_DESCRIPTION,
        url = BuildConfig.PROJECT_WEBSITE
)
public class VelocityFlectonePulseLoader implements Supplier<VelocityFlectonePulseLoader> {

    private static final String JAR_NAME = "flectonepulse-bukkit.jarinjar";
    private static final String BOOTSTRAP_CLASS = "net.flectone.pulse.VelocityFlectonePulse";

    @Getter
    private final Injector injector;

    private JarInJarClassLoader loader;
    private LoaderBootstrap loaderBootstrap;

    @Inject
    public VelocityFlectonePulseLoader(Injector injector) {
        this.injector = injector;
    }

    @Subscribe
    public void onProxyInitializeEvent(ProxyInitializeEvent event) {
        this.loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);
        this.loaderBootstrap = this.loader.instantiatePlugin(BOOTSTRAP_CLASS, Supplier.class, this);
        this.loaderBootstrap.onLoad();
        this.loaderBootstrap.onEnable();
    }

    @Subscribe
    public void onProxyShutdownEvent(ProxyShutdownEvent event) {
        this.loaderBootstrap.onDisable();
    }

    @Override
    public VelocityFlectonePulseLoader get() {
        return this;
    }

}
