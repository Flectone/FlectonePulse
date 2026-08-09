package net.flectone.pulse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.SneakyThrows;
import net.flectone.pulse.constant.DatabaseType;
import net.flectone.pulse.constant.HookType;
import net.flectone.pulse.dispatcher.EventDispatcher;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.logging.filter.LogFilter;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.lifecycle.DisableEvent;
import net.flectone.pulse.model.event.lifecycle.EnableEvent;
import net.flectone.pulse.model.event.lifecycle.ReloadEvent;
import net.flectone.pulse.persistence.database.Database;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.*;
import net.flectone.pulse.platform.render.TextScreenRender;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.*;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

@Singleton
public class FlectonePulseAPIImpl extends FlectonePulseAPI {

    @Inject
    public FlectonePulseAPIImpl(FlectonePulse instance) {
        setInstance(instance);
    }

    @SneakyThrows
    @Override
    public void onEnable() {
        if (!instance.isReady()) return;

        // get event dispatcher
        EventDispatcher eventDispatcher = instance.get(EventDispatcher.class);

        // call enable init event
        EnableEvent enableInitEvent = eventDispatcher.dispatch(new EnableEvent(EnableEvent.Type.INIT, instance));
        if (enableInitEvent.cancelled()) return;

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

        // enable proxy registry
        instance.get(ProxyRegistry.class).onEnable();

        // register default listeners
        instance.get(ListenerRegistry.class).onEnable();

        // setup filter
        instance.get(LogFilter.class).setFilters(fileFacade.config().logger().filter());

        // test database connection
        instance.get(Database.class).connect();

        // initialize packetevents
        instance.hook(HookType.INIT_PACKET_ADAPTER);

        // get fplayer service
        FPlayerService fPlayerService = instance.get(FPlayerService.class);

        // add console to database and cache
        fPlayerService.addConsole();

        // init modules and their children
        instance.get(ModuleController.class).initialize();

        // reload fplayer service
        fPlayerService.initialize(instance.get(SocialService.class), false);

        // reload metrics service if enabled
        if (fileFacade.config().metrics().enable()) {
            instance.get(MetricsService.class).start();
        }

        // call enable ready event
        EnableEvent enableReadyEvent = eventDispatcher.dispatch(new EnableEvent(EnableEvent.Type.READY, instance));
        if (enableReadyEvent.cancelled()) return;

        // log plugin enabled
        fLogger.logEnabled();
    }

    @Override
    public void onDisable() {
        setDisabling(true);

        instance.hook(HookType.TERMINATE_FAILED_PACKET_ADAPTER);

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
        instance.hook(HookType.CLOSE_UIS);

        // unregister all listeners
        instance.get(ListenerRegistry.class).unregisterAll();

        // disable all modules
        instance.get(ModuleController.class).terminate();

        // get fplayer service
        FPlayerService fPlayerService = instance.get(FPlayerService.class);
        PlaytimeService playtimeService = instance.get(PlaytimeService.class);

        // update and clear all fplayers
        fPlayerService.getPlatformFPlayers().forEach(fPlayer -> {
            fPlayerService.clearAndSave(fPlayer);
            playtimeService.updateLastSession(fPlayer);
        });
        fPlayerService.invalidate();

        // terminate packetevents
        instance.hook(HookType.TERMINATE_PACKET_ADAPTER);

        // disable proxy registry
        instance.get(ProxyRegistry.class).onDisable();

        // disconnect from database
        instance.get(Database.class).disconnect();

        // log plugin disabled
        fLogger.logDisabled();
    }

    @Override
    public void reload() throws ReloadException {
        if (!instance.isReady()) return;

        // get event dispatcher
        EventDispatcher eventDispatcher = instance.get(EventDispatcher.class);

        // start reload event
        ReloadEvent startReloadEvent = eventDispatcher.dispatch(new ReloadEvent(instance, ReloadEvent.Type.START));
        if (startReloadEvent.cancelled()) return;

        // get flogger
        FLogger fLogger = instance.get(FLogger.class);

        // log plugin reloading
        fLogger.logReloading();

        // close all UIs
        instance.hook(HookType.CLOSE_UIS);

        // clear text screens
        instance.get(TextScreenRender.class).clear();

        // get listener registry
        ListenerRegistry listenerRegistry = instance.get(ListenerRegistry.class);

        // save reloadListeners to call them later
        Map<Event.Priority, List<UnaryOperator<Event>>> reloadListeners = listenerRegistry.getPulseListeners(ReloadEvent.class);

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

        // sync task scheduler reload
        if (instance.get(PlatformServerAdapter.class).isPrimaryThread()) {
            taskScheduler.reload();
        } else {
            taskScheduler.runSync(taskScheduler::reload).join();
        }

        // get fplayer service
        FPlayerService fPlayerService = instance.get(FPlayerService.class);

        // invalidate players
        fPlayerService.invalidate();

        // get social service
        SocialService socialService = instance.get(SocialService.class);

        // invalidate social caches
        socialService.invalidate();

        // invalidate cache
        instance.get(CacheRegistry.class).invalidate();

        // get database
        Database database = instance.get(Database.class);

        // save old database type
        DatabaseType oldDatabaseType = database.config().type();

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

        // init proxies
        proxyRegistry.onEnable();

        // register default listeners
        listenerRegistry.onEnable();

        // add console to database and cache
        fPlayerService.addConsole();

        // init modules
        moduleController.initialize();

        // reload fplayer service
        fPlayerService.initialize(socialService,true);

        // reload metrics service if enabled
        if (fileFacade.config().metrics().enable()) {
            instance.get(MetricsService.class).start();
        }

        // end reload event
        ReloadEvent endReloadEvent = eventDispatcher.dispatch(reloadListeners, new ReloadEvent(instance, ReloadEvent.Type.END, reloadException));
        if (endReloadEvent.cancelled()) return;

        // log plugin reloaded
        fLogger.logReloaded();

        // throw reload exception if occurred
        if (reloadException != null) {
            throw reloadException;
        }
    }

}
