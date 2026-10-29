package net.flectone.pulse;

import lombok.Getter;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.util.function.Supplier;

@Plugin(BuildConfig.PROJECT_MOD_ID)
public class SpongeFlectonePulseLoader implements Supplier<SpongeFlectonePulseLoader> {

    private static final String JAR_NAME = "flectonepulse-sponge.jarinjar";
    private static final String BOOTSTRAP_CLASS = "net.flectone.pulse.SpongeFlectonePulse";

    @Getter
    private PluginContainer container;

    private JarInJarClassLoader loader;
    private LoaderBootstrap loaderBootstrap;

    @Listener
    public void onConstructPluginEvent(ConstructPluginEvent event) {
        this.container = event.plugin();
        this.loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);
        this.loaderBootstrap = this.loader.instantiatePlugin(BOOTSTRAP_CLASS, Supplier.class, this);
    }

    @Listener(order = Order.LATE)
    public void onStartingEngineEvent(StartingEngineEvent<Server> event) {
        this.loaderBootstrap.onLoad();
        this.loaderBootstrap.onEnable();
    }

    @Listener
    public void onStoppingEngineEvent(StoppingEngineEvent<Server> event) {
        this.loaderBootstrap.onDisable();
    }

    @Override
    public SpongeFlectonePulseLoader get() {
        return this;
    }

}
