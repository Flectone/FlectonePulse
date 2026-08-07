package net.flectone.pulse;

import lombok.Getter;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.function.Supplier;

@Mod(BuildConfig.PROJECT_MOD_ID)
public class NeoForgeFlectonePulseLoader implements Supplier<NeoForgeFlectonePulseLoader> {

    private static final String JAR_NAME = "flectonepulse-neoforge.jarinjar";
    private static final String BOOTSTRAP_CLASS = "net.flectone.pulse.NeoForgeFlectonePulse";

    @Getter
    private final ModContainer container;

    @Getter
    private final IEventBus eventBus;

    @Getter
    private static LoaderBootstrap loaderBootstrap;

    private JarInJarClassLoader loader;

    public NeoForgeFlectonePulseLoader(IEventBus eventBus, ModContainer modContainer) {
        this.container = modContainer;
        this.eventBus = eventBus;

        if (FMLEnvironment.getDist().isClient()) return;

        this.loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);

        eventBus.addListener(this::onCommonSetup);
    }

    public void onCommonSetup(FMLCommonSetupEvent event) {
        loaderBootstrap = this.loader.instantiatePlugin(BOOTSTRAP_CLASS, Supplier.class, this);
        loaderBootstrap.onLoad();
        loaderBootstrap.onEnable();
    }

    @Override
    public NeoForgeFlectonePulseLoader get() {
        return this;
    }

}
