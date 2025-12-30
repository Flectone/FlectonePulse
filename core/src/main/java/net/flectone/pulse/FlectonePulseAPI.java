package net.flectone.pulse;


import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.SneakyThrows;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.lifecycle.DisableEvent;
import net.flectone.pulse.model.event.lifecycle.EnableEvent;
import net.flectone.pulse.model.event.lifecycle.ReloadEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.platform.controller.DialogController;
import net.flectone.pulse.platform.controller.InventoryController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.*;
import net.flectone.pulse.platform.render.TextScreenRender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.MetricsService;
import net.flectone.pulse.service.MinecraftTranslationService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;

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
public class FlectonePulseAPI  {

    /**
     * The main instance of the FlectonePulse.
     * Provides access to dependency injection and functionality.
     *
     * @see FlectonePulse
     */
    @Getter 
    private static FlectonePulse instance;

    /**
     * Constructs the API wrapper with dependency injection.
     * This constructor is called internally by Google Guice.
     *
     * @param instance the main FlectonePulse implementation instance
     */
    @Inject
    public FlectonePulseAPI(FlectonePulse instance) {
        FlectonePulseAPI.instance = instance;
    }

    /**
     * Configures PacketEvents system properties.
     * Must be called before PacketEvents initialization.
     */
    public static void configurePacketEvents() {
        System.setProperty("packetevents.nbt.default-max-size", "2097152");
    }

    /**
     * Terminates PacketEvents API if initialization failed.
     * Prevents errors when FlectonePulse fails to start.
     */
    public static void terminateFailedPacketEvents() {
        try {
            PacketEventsAPI<?> packetEventsAPI = PacketEvents.getAPI();
            if (!packetEventsAPI.isInitialized()) {
                packetEventsAPI.getInjector().uninject();
            }
        } catch (Exception ignored) {
            // ignore
        }
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

        // init defaults files
        FileFacade fileFacade = instance.get(FileFacade.class);
        fileFacade.reload();

        // init cache
        instance.get(CacheRegistry.class).init();

        FLogger fLogger = instance.get(FLogger.class);

        // set file resolver
        fLogger.setFileFacade(fileFacade);

        // log plugin information
        fLogger.logDescription();

        // load minecraft localizations
        instance.get(MinecraftTranslationService.class).reload();

        // register default listeners
        instance.get(ListenerRegistry.class).registerDefaultListeners();

        // setup filter
        fLogger.setupFilter();

        // test database connection
        instance.get(Database.class).connect();

        // initialize packetevents
        PacketEvents.getAPI().init();

        // reload modules and their children
        instance.get(ModuleController.class).reload();

        // reload fplayer service
        instance.get(FPlayerService.class).reload();

        // enable proxy registry
        instance.get(ProxyRegistry.class).onEnable();

        // reload metrics service if enabled
        if (fileFacade.config().metrics().enable()) {
            instance.get(MetricsService.class).reload();
        }

        // call enable event
        instance.get(EventDispatcher.class).dispatch(new EnableEvent(instance));

        // log plugin enabled
        fLogger.logEnabled();
    }

    /**
     * Shuts down the FlectonePulse .
     * Called automatically by the platform on disable.
     */
    public void onDisable() {
        terminateFailedPacketEvents();

        if (!instance.isReady()) return;

        FLogger fLogger = instance.get(FLogger.class);

        // log plugin disabling
        fLogger.logDisabling();

        // call disable event
        instance.get(EventDispatcher.class).dispatch(new DisableEvent(instance));

        // disable task scheduler
        instance.get(TaskScheduler.class).shutdown();

        // close all open inventories
        instance.get(InventoryController.class).closeAll();
        instance.get(DialogController.class).closeAll();

        // get fplayer service
        FPlayerService fPlayerService = instance.get(FPlayerService.class);

        // update and clear all fplayers
        fPlayerService.getOnlineFPlayers().forEach(fPlayer -> {
            fPlayer.setOnline(false);
            fPlayerService.updateFPlayer(fPlayer);
        });
        fPlayerService.clear();

        // disable all modules
        instance.get(ModuleController.class).terminate();

        // unregister all listeners
        instance.get(ListenerRegistry.class).unregisterAll();

        // terminate packetevents
        PacketEvents.getAPI().terminate();

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

        ReloadException reloadException = null;

        FLogger fLogger = instance.get(FLogger.class);

        // log plugin reloading
        fLogger.logReloading();

        // close all open inventories
        instance.get(InventoryController.class).closeAll();
        instance.get(DialogController.class).closeAll();

        // reload ListenerRegistry and save reloadListeners to call them later
        ListenerRegistry listenerRegistry = instance.get(ListenerRegistry.class);

        Map<Event.Priority, List<UnaryOperator<Event>>> reloadListeners = listenerRegistry.getPulseListeners(ReloadEvent.class);

        listenerRegistry.reload();

        // reload task scheduler
        instance.get(TaskScheduler.class).reload();

        // get file resolver for configuration
        FileFacade fileFacade = instance.get(FileFacade.class);

        // get database
        Database database = instance.get(Database.class);

        // save old database type
        Database.Type oldDatabaseType = database.config().type();

        try {
            // reload configuration files
            fileFacade.reload();
        } catch (Exception e) {
            reloadException = new ReloadException(e);
        }

        // load minecraft localizations
        instance.get(MinecraftTranslationService.class).reload();

        // reload registries
        instance.get(CommandRegistry.class).reload();
        instance.get(PermissionRegistry.class).reload();
        instance.get(ProxyRegistry.class).reload();

        // reload logger with new configuration
        fLogger.setupFilter();

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
        FPlayerService fPlayerService = instance.get(FPlayerService.class);

        // reload fplayer service
        fPlayerService.reload();

        // reload moderation service
        instance.get(ModerationService.class).reload();

        // reload modules and their children
        instance.get(ModuleController.class).reload();

        // clear text screens
        instance.get(TextScreenRender.class).clear();

        // process player load event for all platform fplayers
        EventDispatcher eventDispatcher = instance.get(EventDispatcher.class);
        fPlayerService.getPlatformFPlayers().forEach(fPlayer ->
                eventDispatcher.dispatch(new PlayerLoadEvent(fPlayer, true))
        );

        // reload metrics service if enabled
        if (fileFacade.config().metrics().enable()) {
            instance.get(MetricsService.class).reload();
        }

        // call reload event
        eventDispatcher.dispatch(reloadListeners, new ReloadEvent(instance, reloadException));

        // log plugin reloaded
        fLogger.logReloaded();

        // throw reload exception if occurred
        if (reloadException != null) {
            throw reloadException;
        }
    }
}
