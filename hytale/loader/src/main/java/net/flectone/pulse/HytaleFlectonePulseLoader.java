package net.flectone.pulse;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

public class HytaleFlectonePulseLoader extends JavaPlugin implements Supplier<JavaPlugin> {

    private static final String JAR_NAME = "flectonepulse-hytale.jarinjar";
    private static final String BOOTSTRAP_CLASS = "net.flectone.pulse.HytaleFlectonePulse";

    private JarInJarClassLoader loader;
    private LoaderBootstrap loaderBootstrap;

    public HytaleFlectonePulseLoader(@NonNull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        this.loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);
        this.loaderBootstrap = this.loader.instantiatePlugin(BOOTSTRAP_CLASS, Supplier.class, this);
        this.loaderBootstrap.onLoad();
    }

    @Override
    protected void start() {
        this.loaderBootstrap.onEnable();
    }

    @Override
    protected void shutdown() {
        this.loaderBootstrap.onDisable();
    }

    @Override
    public JavaPlugin get() {
        return this;
    }

}