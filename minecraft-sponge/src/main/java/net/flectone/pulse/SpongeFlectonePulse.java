package net.flectone.pulse;

import com.alessiodp.libby.logging.LogLevel;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.constant.HookType;
import net.flectone.pulse.exception.InjectorNotInitializedException;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.platform.adapter.SpongeLogAdapter;
import net.flectone.pulse.platform.adapter.SpongePacketEventsAdapter;
import net.flectone.pulse.platform.controller.MinecraftDialogController;
import net.flectone.pulse.platform.controller.MinecraftInventoryController;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.resolver.SpongeLibraryResolver;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.plugin.PluginContainer;

import java.util.function.Supplier;

@Getter
@Singleton
public class SpongeFlectonePulse implements FlectonePulse {

    @Setter
    private MinecraftServer minecraftServer;

    private final Supplier<SpongeFlectonePulseLoader> loader;

    private FLogger fLogger;
    private LibraryResolver libraryResolver;
    private SpongePacketEventsAdapter packetEventsAdapter;
    private Injector injector;
    private FileFacade fileFacade;

    private ScheduledTask tickTask;

    public SpongeFlectonePulse(Supplier<SpongeFlectonePulseLoader> loader) {
        this.loader = loader;
    }

    @Override
    public void onLoad() {
        PluginContainer container = getContainer();

        // initialize custom logger
        fLogger = new FLogger(new SpongeLogAdapter(container.logger()), () -> fileFacade);
        fLogger.logEnabling();

        // set up library resolver for dependency loading
        libraryResolver = new SpongeLibraryResolver(fLogger);
        libraryResolver.resolveRepositories();

        try {
            libraryResolver.loadLibraries();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Failed to download library")) {
                fLogger.log(LogLevel.ERROR, "\n\n====================\n A problem occurred while downloading the libraries, perhaps you do not have access to repository. \n Try downloading the libraries manually from https://flectone.net/files/r/FlectonePulse-libraries.zip and extract them into FlectonePulse folder \n====================\n");
            }

            throw e;
        }

        // load PacketEvents
        packetEventsAdapter = new SpongePacketEventsAdapter(container);
        packetEventsAdapter.load();

        try {
            // create guice injector for dependency injection
            injector = Guice.createInjector(Stage.PRODUCTION, new SpongeInjector(this, packetEventsAdapter, libraryResolver, fLogger));
            fileFacade = injector.getInstance(FileFacade.class);
        } catch (Exception e) {
            throwInitException(e);
        }
    }

    @Override
    public void onEnable() {
        if (!isReady()) return;

        // get scheduler
        TaskScheduler taskScheduler = get(TaskScheduler.class);

        // create executor
        taskScheduler.start();

        // update tick
        tickTask = Sponge.server().scheduler().submit(Task.builder()
                .plugin(getContainer())
                .interval(Ticks.of(1))
                .execute(taskScheduler::onTick)
                .build()
        );

        injector.getInstance(FlectonePulseAPIImpl.class).onEnable();
    }

    @Override
    public void onDisable() {
        if (!isReady()) {
            hook(HookType.TERMINATE_FAILED_PACKET_ADAPTER);
            return;
        }

        get(FlectonePulseAPIImpl.class).onDisable();

        if (tickTask != null) {
            tickTask.cancel();
        }
    }

    @Override
    public void reload() throws ReloadException {
        if (!isReady()) return;

        get(FlectonePulseAPIImpl.class).reload();
    }

    @Override
    public @NonNull SpongeFlectonePulseLoader getLoader() {
        return loader.get();
    }

    public @NonNull PluginContainer getContainer() {
        return getLoader().getContainer();
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

}
