package net.flectone.pulse;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import lombok.Getter;
import net.flectone.pulse.constant.HookType;
import net.flectone.pulse.exception.InjectorNotInitializedException;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.platform.adapter.BukkitPacketEventsAdapter;
import net.flectone.pulse.platform.controller.MinecraftDialogController;
import net.flectone.pulse.platform.controller.MinecraftInventoryController;
import net.flectone.pulse.resolver.BukkitLibraryResolver;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.scheduler.TaskScheduler;
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
    private BukkitPacketEventsAdapter packetEventsAdapter;
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
        libraryResolver.resolveRepositories();

        try {
            libraryResolver.loadLibraries();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Failed to download library")) {
                logger.severe("\n\n====================\n A problem occurred while downloading the libraries, perhaps you do not have access to repository. \n Try downloading the libraries manually from https://flectone.net/files/r/FlectonePulse-libraries.zip and extract them into FlectonePulse folder \n====================\n");
            }

            throw e;
        }


        // load PacketEvents
        packetEventsAdapter = new BukkitPacketEventsAdapter(plugin);
        packetEventsAdapter.load();

        try {
            // create guice injector for dependency injection
            injector = Guice.createInjector(Stage.PRODUCTION, new BukkitInjector(this, plugin, packetEventsAdapter, libraryResolver, fLogger));
        } catch (Exception e) {
            throwInitException(e);
        }
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

        get(FlectonePulseAPIImpl.class).onEnable();
    }

    @Override
    public void onDisable() {
        if (!isReady()) {
            hook(HookType.TERMINATE_FAILED_PACKET_ADAPTER);
            return;
        }

        get(FlectonePulseAPIImpl.class).onDisable();

        // cancel custom tasks
        injector.getInstance(com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler.class).cancelTasks(loader.get());
    }

    @Override
    public void hook(HookType type, Object... args) {
        try {
            switch (type) {
                case INIT_PACKET_ADAPTER -> packetEventsAdapter.init();
                case TERMINATE_FAILED_PACKET_ADAPTER -> packetEventsAdapter.terminateFailed();
                case TERMINATE_PACKET_ADAPTER -> packetEventsAdapter.terminate();
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

        get(FlectonePulseAPIImpl.class).reload();
    }

    @Override
    public @NonNull BukkitFlectonePulseLoader getLoader() {
        return loader.get();
    }

    @Override
    public <T> T get(Class<T> type) {
        if (!isReady()) {
            throw new InjectorNotInitializedException();
        }

        return injector.getInstance(type);
    }

    @Override
    public boolean isReady() {
        return injector != null;
    }

}