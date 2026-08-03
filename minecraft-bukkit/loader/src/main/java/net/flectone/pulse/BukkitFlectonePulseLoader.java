package net.flectone.pulse;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Supplier;

public class BukkitFlectonePulseLoader extends JavaPlugin implements Supplier<JavaPlugin> {

    private static final String JAR_NAME = "flectonepulse-bukkit.jarinjar";
    private static final String BOOTSTRAP_CLASS = "net.flectone.pulse.BukkitFlectonePulse";

    private JarInJarClassLoader loader;
    private LoaderBoostrap loaderBoostrap;

    @Override
    public void onLoad() {
        this.loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);
        this.loaderBoostrap = this.loader.instantiatePlugin(BOOTSTRAP_CLASS, Supplier.class, this);
        this.loaderBoostrap.onLoad();
    }

    @Override
    public void onEnable() {
        this.loaderBoostrap.onEnable();
    }

    @Override
    public void onDisable() {
        this.loaderBoostrap.onDisable();
    }

    @Override
    public JavaPlugin get() {
        return this;
    }

}