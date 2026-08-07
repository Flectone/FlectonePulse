package net.flectone.pulse;

import net.md_5.bungee.api.plugin.Plugin;

import java.util.function.Supplier;

public class BungeecordFlectonePulseLoader extends Plugin implements Supplier<BungeecordFlectonePulseLoader> {

    private static final String JAR_NAME = "flectonepulse-bukkit.jarinjar";
    private static final String BOOTSTRAP_CLASS = "net.flectone.pulse.BungeecordFlectonePulse";

    private JarInJarClassLoader loader;
    private LoaderBootstrap loaderBootstrap;

    @Override
    public void onEnable() {
        this.loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);
        this.loaderBootstrap = this.loader.instantiatePlugin(BOOTSTRAP_CLASS, Supplier.class, this);
        this.loaderBootstrap.onLoad();
        this.loaderBootstrap.onEnable();
    }

    @Override
    public void onDisable() {
        this.loaderBootstrap.onDisable();
    }

    @Override
    public BungeecordFlectonePulseLoader get() {
        return this;
    }

}
