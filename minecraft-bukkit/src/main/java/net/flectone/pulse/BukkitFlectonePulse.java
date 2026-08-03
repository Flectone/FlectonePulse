package net.flectone.pulse;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.platform.controller.MinecraftDialogController;
import net.flectone.pulse.platform.controller.MinecraftInventoryController;
import net.flectone.pulse.processing.resolver.BukkitLibraryResolver;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.util.constant.HookType;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;
import java.util.logging.Logger;

@Getter
@Singleton
public class BukkitFlectonePulse implements FlectonePulse {

    private final Supplier<BukkitFlectonePulseLoader> loader;

    private FLogger fLogger;
    private LibraryResolver libraryResolver;
    private Injector injector;

    public BukkitFlectonePulse(Supplier<BukkitFlectonePulseLoader> loader) {
        this.loader = loader;
    }

    @Override
    public void onLoad() {
        JavaPlugin plugin = getLoader();
        Logger logger = plugin.getLogger();

        // initialize custom logger
        fLogger = new FLogger(
                logger::log,
                () -> injector == null ? null : injector.getInstance(FileFacade.class)
        );
        fLogger.logEnabling();

        // set up library resolver for dependency loading
        libraryResolver = new BukkitLibraryResolver(plugin);
        libraryResolver.addLibraries();
        libraryResolver.resolveRepositories();
        libraryResolver.loadLibraries();

        // configure packetevents api
        System.setProperty("packetevents.nbt.default-max-size", "2097152");
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(plugin));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false).checkForUpdates(false).debug(false);

        try {
            // create guice injector for dependency injection
            injector = Guice.createInjector(Stage.PRODUCTION, new BukkitInjector(this, plugin, libraryResolver, fLogger));
        } catch (Exception e) {
            throwInitException(e);
        }

        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        if (!isReady()) {
            Bukkit.getPluginManager().disablePlugin(loader.get());
            return;
        }

        // get scheduler
        TaskScheduler taskScheduler = get(TaskScheduler.class);

        // create executor
        taskScheduler.start();

        // update tick
        injector.getInstance(com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler.class).runTaskTimer(taskScheduler::onTick, 1L, 1L);

        get(FlectonePulseAPI.class).onEnable();
    }

    @Override
    public void onDisable() {
        if (!isReady()) {
            hook(HookType.TERMINATE_FAILED_PACKET_ADAPTER);
            return;
        }

        get(FlectonePulseAPI.class).onDisable();

        // cancel custom tasks
        injector.getInstance(com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler.class).cancelTasks(loader.get());
    }

    @Override
    public void hook(HookType type, Object... args) {
        try {
            switch (type) {
                case INIT_PACKET_ADAPTER -> PacketEvents.getAPI().init();
                case TERMINATE_FAILED_PACKET_ADAPTER -> {
                    try {
                        // check PacketEvents class
                        Class.forName("com.github.retrooper.packetevents.PacketEvents");

                        PacketEventsAPI<?> packetEventsAPI = PacketEvents.getAPI();
                        if (!packetEventsAPI.isInitialized()) {
                            packetEventsAPI.getInjector().uninject();
                        }
                    } catch (Exception _) {
                        // ignore
                    }
                }
                case TERMINATE_PACKET_ADAPTER -> PacketEvents.getAPI().terminate();
                case CLOSE_UIS -> {
                    injector.getInstance(MinecraftInventoryController.class).closeAll();
                    injector.getInstance(MinecraftDialogController.class).closeAll();
                }
            }
        } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
            fLogger.warning("Hook % type called with invalid arguments: %s", type, e.getMessage());
        }
    }

    @Override
    public void reload() throws ReloadException {
        if (!isReady()) return;

        get(FlectonePulseAPI.class).reload();
    }

    @Override
    public @NonNull BukkitFlectonePulseLoader getLoader() {
        return loader.get();
    }

}