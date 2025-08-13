package net.flectone.pulse;


import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.module.Module;
import net.flectone.pulse.platform.controller.DialogController;
import net.flectone.pulse.platform.controller.InventoryController;
import net.flectone.pulse.platform.registry.CommandRegistry;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.PermissionRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
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
        if (!instance.isInitialized()) return;

        FLogger fLogger = instance.get(FLogger.class);

        // log plugin information
        fLogger.logPluginInfo();

        // register default listeners
        instance.get(ListenerRegistry.class).registerDefaultListeners();

        try {
            // connect to database
            instance.get(Database.class).connect();
        } catch (Exception e) {
            fLogger.warning(e);
        }

        // get file resolver for configuration
        FileResolver fileResolver = instance.get(FileResolver.class);

        // reload logger with new configuration
        fLogger.reload(fileResolver.getConfig().getLogFilter());

        // initialize packetevents
        PacketEvents.getAPI().init();

        // reload modules and their children
        instance.get(net.flectone.pulse.module.Module.class).reloadWithChildren();

        // reload fplayer service
        instance.get(FPlayerService.class).reload();

        // enable proxy registry
        instance.get(ProxyRegistry.class).onEnable();

        // reload metrics service if enabled
        if (fileResolver.getConfig().isMetrics()) {
            instance.get(MetricsService.class).reload();
        }

        // log plugin enabled
        fLogger.logEnabled();
    }

    public void onDisable() {
        if (!instance.isInitialized()) {
            // terminate packetevents if injector is not initialized
            PacketEvents.getAPI().terminate();
            return;
        }

        FLogger fLogger = instance.get(FLogger.class);

        // log plugin disabling
        fLogger.logDisabling();

        // send metrics data if enabled
        if (instance.get(FileResolver.class).getConfig().isMetrics()) {
            instance.get(MetricsService.class).send();
        }

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

        // disable task scheduler
        TaskScheduler taskScheduler = instance.get(TaskScheduler.class);
        taskScheduler.setDisabled(true);

        // disable all modules
        instance.get(net.flectone.pulse.module.Module.class).terminate();

        // unregister all listeners
        instance.get(ListenerRegistry.class).unregisterAll();

        // terminate packetevents
        PacketEvents.getAPI().terminate();

        // disable proxy registry
        instance.get(ProxyRegistry.class).onDisable();

        // disconnect from database
        instance.get(Database.class).disconnect();

        // reload task scheduler
        taskScheduler.reload();

        // log plugin disabled
        fLogger.logDisabled();
    }

    public void reload() throws ReloadException {
        if (!instance.isInitialized()) return;

        ReloadException reloadException = null;

        FLogger fLogger = instance.get(FLogger.class);

        // log plugin reloading
        fLogger.logReloading();

        // get file resolver for configuration
        FileResolver fileResolver = instance.get(FileResolver.class);

        try {
            // reload configuration files
            fileResolver.reload();
        } catch (Exception e) {
            reloadException = new ReloadException(e.getMessage(), e);
        }

        // close all open inventories
        instance.get(InventoryController.class).closeAll();
        instance.get(DialogController.class).closeAll();

        // reload registries
        instance.get(CommandRegistry.class).reload();
        instance.get(ListenerRegistry.class).reload();
        instance.get(PermissionRegistry.class).reload();
        instance.get(ProxyRegistry.class).reload();

        // reload task scheduler
        instance.get(TaskScheduler.class).reload();

        // reload logger with new configuration
        fLogger.reload(fileResolver.getConfig().getLogFilter());

        // get fplayer service
        FPlayerService fPlayerService = instance.get(FPlayerService.class);

        try {
            // disconnect and reconnect to database
            instance.get(Database.class).disconnect();
            instance.get(Database.class).connect();

            // reload fplayer service
            fPlayerService.reload();

        } catch (Exception e) {
            reloadException = new ReloadException(e.getMessage(), e);
        }

        // reload moderation service
        instance.get(ModerationService.class).reload();

        // reload modules and their children
        instance.get(Module.class).reloadWithChildren();

        // process player load event for all platform fplayers
        EventDispatcher eventDispatcher = instance.get(EventDispatcher.class);
        fPlayerService.getPlatformFPlayers().forEach(fPlayer ->
                eventDispatcher.dispatch(new PlayerLoadEvent(fPlayer, true))
        );

        // reload metrics service if enabled
        if (fileResolver.getConfig().isMetrics()) {
            instance.get(MetricsService.class).reload();
        }

        // log plugin reloaded
        fLogger.logReloaded();

        // throw reload exception if occurred
        if (reloadException != null) {
            throw reloadException;
        }
    }
}