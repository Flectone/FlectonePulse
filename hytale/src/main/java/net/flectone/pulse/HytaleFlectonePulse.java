package net.flectone.pulse;

import com.alessiodp.libby.LibraryManager;
import com.alessiodp.libby.StandaloneLibraryManager;
import com.alessiodp.libby.logging.LogLevel;
import com.alessiodp.libby.logging.adapters.LogAdapter;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import lombok.Getter;
import lombok.SneakyThrows;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.scheduler.HytaleTaskScheduler;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.lifecycle.DisableEvent;
import net.flectone.pulse.model.event.lifecycle.EnableEvent;
import net.flectone.pulse.model.event.lifecycle.ReloadEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.*;
import net.flectone.pulse.platform.render.TextScreenRender;
import net.flectone.pulse.processing.resolver.HytaleLibraryResolver;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.HytaleTranslationService;
import net.flectone.pulse.service.MetricsService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.flectone.pulse.util.logging.filter.LogFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.logging.Level;

@Getter
@Singleton
public class HytaleFlectonePulse extends JavaPlugin implements FlectonePulse {

    private final Path projectPath;

    private FLogger fLogger;
    private Injector injector;

    public HytaleFlectonePulse(@NonNull JavaPluginInit init) {
        super(init);

        projectPath = init.getFile().getParent().resolve("FlectonePulse");
    }

    @Override
    protected void setup() {
        // initialize custom logger
        HytaleLogger hytaleLogger = this.getLogger();
        fLogger = new FLogger(logRecord -> hytaleLogger.at(logRecord.getLevel()).log(logRecord.getMessage()));
        fLogger.logEnabling();

        // set up library resolver for dependency loading
        LibraryManager libraryManager = getLibraryManager(hytaleLogger);
        LibraryResolver libraryResolver = new HytaleLibraryResolver(libraryManager);
        libraryResolver.addLibraries();
        libraryResolver.resolveRepositories();
        libraryResolver.loadLibraries();

        // create guice injector for dependency injection
        injector = Guice.createInjector(new HytaleInjector(this, projectPath, libraryResolver, fLogger));

        HytaleTaskScheduler hytaleTaskScheduler = injector.getInstance(HytaleTaskScheduler.class);
        HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(hytaleTaskScheduler::onTick, 50L, 50L, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void start() {
        onEnable();
    }

    @Override
    protected void shutdown() {
        onDisable();
    }

    @NotNull
    private LibraryManager getLibraryManager(HytaleLogger hytaleLogger) {
        LogAdapter logAdapter = new LogAdapter() {
            @Override
            public void log(@NotNull LogLevel logLevel, @Nullable String s) {
                hytaleLogger.at(Level.parse(logLevel.name())).log(s);
            }

            @Override
            public void log(@NotNull LogLevel logLevel, @Nullable String s, @Nullable Throwable throwable) {
                hytaleLogger.at(Level.parse(logLevel.name())).log(s, throwable);
            }
        };

        return new StandaloneLibraryManager(logAdapter, projectPath, "libraries");
    }

    @Override
    public <T> T get(Class<T> type) {
        if (!isReady()) {
            throw new IllegalStateException("FlectonePulse not initialized yet");
        }

        return injector.getInstance(type);
    }

    @Override
    public boolean isReady() {
        return injector != null;
    }

    @Override
    @SneakyThrows
    public void onEnable() {
        if (!isReady()) return;

        // init defaults files
        FileFacade fileFacade = get(FileFacade.class);
        fileFacade.reload();

        // init cache
        get(CacheRegistry.class).init();

        // set file resolver
        fLogger.setFileFacade(fileFacade);

        // log plugin information
        fLogger.logDescription();

        // load minecraft localizations
        get(HytaleTranslationService.class).reload();

        // register default listeners
        get(ListenerRegistry.class).registerDefaultListeners();

        // setup filter
        injector.getInstance(LogFilter.class).setFilters(fileFacade.config().logger().filter());

        // test database connection
        get(Database.class).connect();

        // reload modules and their children
        get(ModuleController.class).reload();

        // reload fplayer service
        get(FPlayerService.class).reload();

        // enable proxy registry
        get(ProxyRegistry.class).onEnable();

        // reload metrics service if enabled
        if (fileFacade.config().metrics().enable()) {
            get(MetricsService.class).reload();
        }

        // call enable event
        get(EventDispatcher.class).dispatch(new EnableEvent(this));

        // log plugin enabled
        fLogger.logEnabled();
    }

    @Override
    public void onDisable() {
        if (!isReady()) return;

        // log plugin disabling
        fLogger.logDisabling();

        // call disable event
        get(EventDispatcher.class).dispatch(new DisableEvent(this));

        // disable task scheduler
        get(TaskScheduler.class).shutdown();

        // get fplayer service
        FPlayerService fPlayerService = get(FPlayerService.class);

        // update and clear all fplayers
        fPlayerService.getOnlineFPlayers().forEach(fPlayer -> {
            fPlayer.setOnline(false);
            fPlayerService.updateFPlayer(fPlayer);
        });
        fPlayerService.clear();

        // disable all modules
        get(ModuleController.class).terminate();

        // unregister all listeners
        get(ListenerRegistry.class).unregisterAll();

        // disable proxy registry
        get(ProxyRegistry.class).onDisable();

        // disconnect from database
        get(Database.class).disconnect();

        // log plugin disabled
        fLogger.logDisabled();
    }

    @Override
    public void reload() throws ReloadException {
        if (!isReady()) return;

        ReloadException reloadException = null;

        // log plugin reloading
        fLogger.logReloading();

        // reload ListenerRegistry and save reloadListeners to call them later
        ListenerRegistry listenerRegistry = get(ListenerRegistry.class);

        Map<Event.Priority, List<UnaryOperator<Event>>> reloadListeners = listenerRegistry.getPulseListeners(ReloadEvent.class);

        listenerRegistry.reload();

        // reload task scheduler
        get(TaskScheduler.class).reload();

        // get file resolver for configuration
        FileFacade fileFacade = get(FileFacade.class);

        // get database
        Database database = get(Database.class);

        // save old database type
        Database.Type oldDatabaseType = database.config().type();

        try {
            // reload configuration files
            fileFacade.reload();
        } catch (Exception e) {
            reloadException = new ReloadException(e);
        }

        // load minecraft localizations
        get(HytaleTranslationService.class).reload();

        // reload registries
        get(CommandRegistry.class).reload();
        get(PermissionRegistry.class).reload();
        get(ProxyRegistry.class).reload();

        // reload logger filters
        injector.getInstance(LogFilter.class).setFilters(fileFacade.config().logger().filter());

        // terminate database
        database.disconnect();

        // test new database connection
        try {
            database.connect();
        } catch (Exception e) {
            if (reloadException == null) {
                reloadException = new ReloadException(e);
            }

            // try to connect to old database
            if (database.config().type() != oldDatabaseType) {
                fileFacade.updateFilePack(filePack ->
                        filePack.withConfig(
                                filePack.config().withDatabase(
                                        filePack.config().database().withType(oldDatabaseType)
                                )
                        )
                );

                try {
                    database.connect();
                } catch (Exception ignored) {
                    throw reloadException;
                }
            }
        }

        // get fplayer service
        FPlayerService fPlayerService = get(FPlayerService.class);

        // reload fplayer service
        fPlayerService.reload();

        // reload moderation service
        get(ModerationService.class).reload();

        // reload modules and their children
        get(ModuleController.class).reload();

        // clear text screens
        get(TextScreenRender.class).clear();

        // process player load event for all platform fplayers
        EventDispatcher eventDispatcher = get(EventDispatcher.class);
        fPlayerService.getPlatformFPlayers().forEach(fPlayer ->
                eventDispatcher.dispatch(new PlayerLoadEvent(fPlayer, true))
        );

        // reload metrics service if enabled
        if (fileFacade.config().metrics().enable()) {
            get(MetricsService.class).reload();
        }

        // call reload event
        eventDispatcher.dispatch(reloadListeners, new ReloadEvent(this, reloadException));

        // log plugin reloaded
        fLogger.logReloaded();

        // throw reload exception if occurred
        if (reloadException != null) {
            throw reloadException;
        }
    }

}
