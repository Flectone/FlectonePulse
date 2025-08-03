package net.flectone.pulse;


import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.platform.controller.InventoryController;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.module.Module;
import net.flectone.pulse.platform.registry.CommandRegistry;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.PermissionRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.MetricsService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class FlectonePulseAPI  {

    @Getter private static FlectonePulse instance;

    @Inject
    public FlectonePulseAPI(FlectonePulse instance) {
        FlectonePulseAPI.instance = instance;
    }

    public void onEnable() {
        Injector injector = instance.getInjector();
        if (injector == null) return;

        FLogger fLogger = injector.getInstance(FLogger.class);

        // log plugin information
        fLogger.logPluginInfo();

        // register default listeners
        injector.getInstance(ListenerRegistry.class).registerDefaultListeners();

        try {
            // connect to database
            injector.getInstance(Database.class).connect();
        } catch (Exception e) {
            fLogger.warning(e);
        }

        // get file resolver for configuration
        FileResolver fileResolver = injector.getInstance(FileResolver.class);

        // reload logger with new configuration
        fLogger.reload(fileResolver.getConfig().getLogFilter());

        // initialize packetevents
        PacketEvents.getAPI().init();

        // reload modules and their children
        injector.getInstance(net.flectone.pulse.module.Module.class).reloadWithChildren();

        // reload fplayer service
        injector.getInstance(FPlayerService.class).reload();

        // enable proxy registry
        injector.getInstance(ProxyRegistry.class).onEnable();

        // reload metrics service if enabled
        if (fileResolver.getConfig().isMetrics()) {
            injector.getInstance(MetricsService.class).reload();
        }

        // log plugin enabled
        fLogger.logEnabled();
    }

    public void onDisable() {
        Injector injector = instance.getInjector();
        if (injector == null) {
            // terminate packetevents if injector is not initialized
            PacketEvents.getAPI().terminate();
            return;
        }

        FLogger fLogger = injector.getInstance(FLogger.class);

        // log plugin disabling
        fLogger.logDisabling();

        // send metrics data if enabled
        if (injector.getInstance(FileResolver.class).getConfig().isMetrics()) {
            injector.getInstance(MetricsService.class).send();
        }

        // close all open inventories
        injector.getInstance(InventoryController.class).closeAll();

        // get fplayer service
        FPlayerService fPlayerService = injector.getInstance(FPlayerService.class);

        // update and clear all fplayers
        fPlayerService.getFPlayers().forEach(fPlayer -> {
            fPlayer.setOnline(false);
            fPlayerService.updateFPlayer(fPlayer);
        });
        fPlayerService.clear();

        // disable task scheduler
        TaskScheduler taskScheduler = injector.getInstance(TaskScheduler.class);
        taskScheduler.setDisabled(true);

        // disable all modules
        injector.getInstance(net.flectone.pulse.module.Module.class).terminate();

        // unregister all listeners
        injector.getInstance(ListenerRegistry.class).unregisterAll();

        // terminate packetevents
        PacketEvents.getAPI().terminate();

        // disable proxy registry
        injector.getInstance(ProxyRegistry.class).onDisable();

        // disconnect from database
        injector.getInstance(Database.class).disconnect();

        // reload task scheduler
        taskScheduler.reload();

        // log plugin disabled
        fLogger.logDisabled();
    }

    public void reload() throws ReloadException {
        Injector injector = instance.getInjector();
        if (injector == null) return;

        ReloadException reloadException = null;

        FLogger fLogger = injector.getInstance(FLogger.class);

        // log plugin reloading
        fLogger.logReloading();

        // get file resolver for configuration
        FileResolver fileResolver = injector.getInstance(FileResolver.class);

        try {
            // reload configuration files
            fileResolver.reload();
        } catch (Exception e) {
            reloadException = new ReloadException(e.getMessage(), e);
        }

        // close all open inventories
        injector.getInstance(InventoryController.class).closeAll();

        // reload registries
        injector.getInstance(CommandRegistry.class).reload();
        injector.getInstance(ListenerRegistry.class).reload();
        injector.getInstance(PermissionRegistry.class).reload();
        injector.getInstance(ProxyRegistry.class).reload();

        // reload task scheduler
        injector.getInstance(TaskScheduler.class).reload();

        // reload logger with new configuration
        fLogger.reload(fileResolver.getConfig().getLogFilter());

        // get fplayer service
        FPlayerService fPlayerService = injector.getInstance(FPlayerService.class);

        try {
            // disconnect and reconnect to database
            injector.getInstance(Database.class).disconnect();
            injector.getInstance(Database.class).connect();

            // reload fplayer service
            fPlayerService.reload();

        } catch (Exception e) {
            reloadException = new ReloadException(e.getMessage(), e);
        }

        // reload moderation service
        injector.getInstance(ModerationService.class).reload();

        // reload modules and their children
        injector.getInstance(Module.class).reloadWithChildren();

        // process player load event for all platform fplayers
        EventDispatcher eventDispatcher = injector.getInstance(EventDispatcher.class);
        fPlayerService.getPlatformFPlayers().forEach(fPlayer ->
                eventDispatcher.dispatch(new PlayerLoadEvent(fPlayer))
        );

        // reload metrics service if enabled
        if (fileResolver.getConfig().isMetrics()) {
            injector.getInstance(MetricsService.class).reload();
        }

        // log plugin reloaded
        fLogger.logReloaded();

        // throw reload exception if occurred
        if (reloadException != null) {
            throw reloadException;
        }
    }
}