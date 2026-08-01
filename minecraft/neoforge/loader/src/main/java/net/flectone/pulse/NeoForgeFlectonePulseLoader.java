package net.flectone.pulse;

import lombok.Getter;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.function.Supplier;

@Mod(BuildConfig.PROJECT_MOD_ID)
public class NeoForgeFlectonePulseLoader implements Supplier<ModContainer> {

    private static final String JAR_NAME = "flectonepulsedev-neoforge.jarinjar";
    private static final String BOOTSTRAP_CLASS = "net.flectone.pulse.NeoForgeFlectonePulse";

    private final ModContainer container;

    private JarInJarClassLoader loader;

    @Getter
    private static LoaderBootstrap plugin;

    public NeoForgeFlectonePulseLoader(IEventBus modEventBus, ModContainer modContainer) {
        this.container = modContainer;

        if (FMLEnvironment.getDist().isClient()) return;

        this.loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);

        modEventBus.addListener(this::onCommonSetup);
    }

    public void onCommonSetup(FMLCommonSetupEvent event) {
        this.plugin = this.loader.instantiatePlugin(BOOTSTRAP_CLASS, Supplier.class, this);
        this.plugin.onLoad();
    }

    @Override
    public ModContainer get() {
        return this.container;
    }
}
