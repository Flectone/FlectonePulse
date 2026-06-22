package net.flectone.pulse;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.platform.controller.MinecraftDialogController;
import net.flectone.pulse.platform.controller.MinecraftInventoryController;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.NeoForgeLibraryResolver;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Singleton
@Mod(BuildConfig.PROJECT_MOD_ID)
public class NeoForgeFlectonePulse implements FlectonePulse {

    @Setter
    private MinecraftServer minecraftServer;

    private final IEventBus modEventBus;

    @Getter
    private final ModContainer modContainer;

    private FLogger fLogger;
    private Injector injector;

    public NeoForgeFlectonePulse(IEventBus modEventBus, ModContainer modContainer) {
        this.modEventBus = modEventBus;
        this.modContainer = modContainer;

        // initialize custom logger
        Logger logger = LoggerFactory.getLogger(BuildConfig.PROJECT_MOD_ID);
        fLogger = new FLogger(
                logRecord -> logger.info(logRecord.getMessage()),
                () -> injector == null ? null : injector.getInstance(FileFacade.class)
        );
        fLogger.logEnabling();

        // set up library resolver for dependency loading
        LibraryResolver libraryResolver = new NeoForgeLibraryResolver(modContainer, logger);
        libraryResolver.addLibraries();
        libraryResolver.resolveRepositories();
        libraryResolver.loadLibraries();

        try {
            // create guice injector for dependency injection
            injector = Guice.createInjector(Stage.PRODUCTION, new NeoForgeInjector(this, libraryResolver, fLogger));
        } catch (Exception e) {
            throwInitException(e);
        }

        // we need to call enable right now, because the commands must be registered before server is fully started
        onEnable();
    }

    @Override
    public void onEnable() {
        if (!isReady()) return;

        // get scheduler
        TaskScheduler taskScheduler = get(TaskScheduler.class);

        // create executor
        taskScheduler.start();

        // update tick
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Pre.class, _ -> taskScheduler.onTick());

        injector.getInstance(FlectonePulseAPI.class).onEnable();
    }

    @Override
    public void onDisable() {
        if (!isReady()) {
            terminateFailedPacketAdapter();
            return;
        }

        get(FlectonePulseAPI.class).onDisable();
    }

    @Override
    public void reload() throws ReloadException {
        if (!isReady()) return;

        get(FlectonePulseAPI.class).reload();
    }

    @Override
    public void initPacketAdapter() {
        PacketEvents.getAPI().init();
    }

    @Override
    public void terminateFailedPacketAdapter() {
        try {
            PacketEventsAPI<?> packetEventsAPI = PacketEvents.getAPI();
            if (!packetEventsAPI.isInitialized()) {
                packetEventsAPI.getInjector().uninject();
            }
        } catch (Exception _) {
            // ignore
        }
    }

    @Override
    public void terminatePacketAdapter() {
        PacketEvents.getAPI().terminate();
    }

    @Override
    public void closeUIs() {
        // close all open inventories
        injector.getInstance(MinecraftInventoryController.class).closeAll();
        injector.getInstance(MinecraftDialogController.class).closeAll();
    }
}
