package net.flectone.pulse;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.lifecycle.DisableEvent;
import net.flectone.pulse.model.event.lifecycle.EnableEvent;
import net.flectone.pulse.model.event.lifecycle.EndReloadEvent;
import net.flectone.pulse.model.event.lifecycle.StartReloadEvent;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.*;
import net.flectone.pulse.platform.render.TextScreenRender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.MetricsService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.service.TranslationService;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.flectone.pulse.util.logging.filter.LogFilter;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * API entry point for FlectonePulse plugin integration.
 * Provides static access to the main {@link FlectonePulse} instance and lifecycle management.
 *
 * @see FlectonePulse
 * @since 0.1.0
 */
@Singleton
public class FlectonePulseAPI {

    /**
     * The main instance of the FlectonePulse.
     * Provides access to dependency injection and functionality.
     *
     * @see FlectonePulse
     */
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static FlectonePulse instance;

    /**
     * Indicates whether FlectonePulse is currently in the process of being disabled.
     *
     */
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static boolean disabling;

    /**
     * Constructs the API wrapper with dependency injection.
     * This constructor is called internally by Google Guice.
     *
     * @param instance the main FlectonePulse implementation instance
     */
    @Inject
    public FlectonePulseAPI(FlectonePulse instance) {
        setInstance(instance);
    }

    /**
     * Initializes and enables the FlectonePulse .
     * Called automatically by the platform on enable.
     *
     * @throws IllegalStateException if called when is already enabled
     */
    @SneakyThrows
    public void onEnable() {
        if (!instance.isReady()) return;

        // call enable event
        EnableEvent enableEvent = instance.get(EventDispatcher.class).dispatch(new EnableEvent(instance));
        if (enableEvent.cancelled()) return;

        // get configs
        FileFacade fileFacade = instance.get(FileFacade.class);

        // get fLogger
        FLogger fLogger = instance.get(FLogger.class);

        // log plugin information
        fLogger.logDescription();

        // load platform localizations
        instance.get(TranslationService.class).reload();

        // init command registry
        instance.get(CommandRegistry.class).init();

        // register default listeners
        instance.get(ListenerRegistry.class).registerDefaultListeners();

        // setup filter
        instance.get(LogFilter.class).setFilters(fileFacade.config().logger().filter());

        // test database connection
        instance.get(Database.class).connect();

        // initialize packetevents
        instance.initPacketAdapter();

        // init modules and their children
        instance.get(ModuleController.class).initialize();

        // reload fplayer service
        instance.get(FPlayerService.class).initialize(false);

        // enable proxy registry
        instance.get(ProxyRegistry.class).onEnable();

        // reload metrics service if enabled
        if (fileFacade.config().metrics().enable()) {
            instance.get(MetricsService.class).start();
        }

        // log plugin enabled
        fLogger.logEnabled();
    }

    /**
     * Shuts down the FlectonePulse .
     * Called automatically by the platform on disable.
     */
    public void onDisable() {
        setDisabling(true);

        instance.terminateFailedPacketAdapter();

        if (!instance.isReady()) return;

        // call disable event
        DisableEvent disableEvent = instance.get(EventDispatcher.class).dispatch(new DisableEvent(instance));
        if (disableEvent.cancelled()) return;

        // get flogger
        FLogger fLogger = instance.get(FLogger.class);

        // log plugin disabling
        fLogger.logDisabling();

        // disable task scheduler (it can no longer be used on disable)
        instance.get(TaskScheduler.class).shutdown();

        // close all open inventories
        instance.closeUIs();

        // unregister all listeners
        instance.get(ListenerRegistry.class).unregisterAll();

        // disable all modules
        instance.get(ModuleController.class).terminate();

        // get fplayer service
        FPlayerService fPlayerService = instance.get(FPlayerService.class);

        // update and clear all fplayers
        fPlayerService.getPlatformFPlayers().forEach(fPlayerService::clearAndSave);
        fPlayerService.invalidate();

        // terminate packetevents
        instance.terminatePacketAdapter();

        // disable proxy registry
        instance.get(ProxyRegistry.class).onDisable();

        // disconnect from database
        instance.get(Database.class).disconnect();

        // log plugin disabled
        fLogger.logDisabled();
    }

    /**
     * Reloads the plugin configuration and modules at runtime.
     *
     * @throws ReloadException if any error occurs during reload process
     */
    public void reload() throws ReloadException {
        if (!instance.isReady()) return;

        // get event dispatcher
        EventDispatcher eventDispatcher = instance.get(EventDispatcher.class);

        // start reload event
        StartReloadEvent startReloadEvent = eventDispatcher.dispatch(new StartReloadEvent(instance));
        if (startReloadEvent.cancelled()) return;

        // get flogger
        FLogger fLogger = instance.get(FLogger.class);

        // log plugin reloading
        fLogger.logReloading();

        // close all UIs
        instance.closeUIs();

        // clear text screens
        instance.get(TextScreenRender.class).clear();

        // get listener registry
        ListenerRegistry listenerRegistry = instance.get(ListenerRegistry.class);

        // save reloadListeners to call them later
        Map<Event.Priority, List<UnaryOperator<Event>>> reloadListeners = listenerRegistry.getPulseListeners(EndReloadEvent.class);

        // clear listeners and register default listeners
        listenerRegistry.onDisable();

        // clear commands
        instance.get(CommandRegistry.class).onDisable();

        // clear permissions
        instance.get(PermissionRegistry.class).onDisable();

        // reload moderation service
        instance.get(ModerationService.class).invalidate();

        // get module controller
        ModuleController moduleController = instance.get(ModuleController.class);

        // reload modules and their children
        moduleController.terminate();

        // get task scheduler
        TaskScheduler taskScheduler = instance.get(TaskScheduler.class);

        // disable task scheduler
        taskScheduler.shutdown();

        // get fplayer service
        FPlayerService fPlayerService = instance.get(FPlayerService.class);

        // invalidate players
        fPlayerService.invalidate();

        // invalidate cache
        instance.get(CacheRegistry.class).invalidate();

        // get database
        Database database = instance.get(Database.class);

        // save old database type
        Database.Type oldDatabaseType = database.config().type();

        // get file resolver for configuration
        FileFacade fileFacade = instance.get(FileFacade.class);

        ReloadException reloadException = null;
        try {
            // reload configuration files
            fileFacade.reload();
        } catch (Exception e) {
            reloadException = new ReloadException(e);
        }

        // reload logger filters
        instance.get(LogFilter.class).setFilters(fileFacade.config().logger().filter());

        // get proxy registry
        ProxyRegistry proxyRegistry = instance.get(ProxyRegistry.class);

        // reload registries
        proxyRegistry.onDisable();

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
                } catch (Exception _) {
                    throw reloadException;
                }
            }
        }

        // load minecraft localizations
        instance.get(TranslationService.class).reload();

        // start scheduler
        taskScheduler.start();

        // register default listeners
        listenerRegistry.onEnable();

        // init proxies
        proxyRegistry.onEnable();

        // init modules
        moduleController.initialize();

        // reload fplayer service
        fPlayerService.initialize(true);

        // reload metrics service if enabled
        if (fileFacade.config().metrics().enable()) {
            instance.get(MetricsService.class).start();
        }

        // end reload event
        EndReloadEvent endReloadEvent = eventDispatcher.dispatch(reloadListeners, new EndReloadEvent(instance, reloadException));
        if (endReloadEvent.cancelled()) return;

        // log plugin reloaded
        fLogger.logReloaded();

        // throw reload exception if occurred
        if (reloadException != null) {
            throw reloadException;
        }
    }

}
