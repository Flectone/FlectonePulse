package net.flectone.pulse;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import net.flectone.pulse.controller.InventoryController;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.exception.ReloadException;
import net.flectone.pulse.module.Module;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.registry.*;
import net.flectone.pulse.resolver.BukkitLibraryResolver;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.scheduler.BukkitTaskScheduler;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.MetricsService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Singleton
public class BukkitFlectonePulse extends JavaPlugin implements FlectonePulse {

    private FLogger fLogger;
    private LibraryResolver libraryResolver;
    private Injector injector;

    @Override
    public void onLoad() {
        // initialize custom logger
        fLogger = new FLogger(this.getLogger());
        fLogger.enableFilter();
        fLogger.logEnabling();

        // set up library resolver for dependency loading
        libraryResolver = new BukkitLibraryResolver(this, fLogger);
        libraryResolver.addLibraries();
        libraryResolver.resolveRepositories();
        libraryResolver.loadLibraries();

        // configure packetevents api
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings()
                .reEncodeByDefault(false)
                .checkForUpdates(false)
                .debug(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        try {
            // create guice injector for dependency injection
            injector = Guice.createInjector(new BukkitInjector(this, this, libraryResolver, fLogger));
        } catch (RuntimeException e) {
            fLogger.warning("FAILED TO ENABLE");
            fLogger.warning(e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // log plugin information
        fLogger.logPluginInfo();

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
        injector.getInstance(Module.class).reloadWithChildren();

        // reload fplayer service
        injector.getInstance(FPlayerService.class).reload();

        // enable proxy registry
        injector.getInstance(ProxyRegistry.class).onEnable();

        // reload metrics service if enabled
        if (fileResolver.getConfig().isMetrics()) {
            injector.getInstance(MetricsService.class).reload();
        }

        // register default listeners
        injector.getInstance(ListenerRegistry.class).registerDefaultListeners();

        // reload disabled tags
        injector.getInstance(MessagePipeline.class).reload();

        // log plugin enabled
        fLogger.logEnabled();
    }

    @Override
    public void onDisable() {
        if (injector == null) {
            // terminate packetevents if injector is not initialized
            PacketEvents.getAPI().terminate();
            return;
        }

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
        BukkitTaskScheduler taskScheduler = injector.getInstance(BukkitTaskScheduler.class);
        taskScheduler.setDisabled(true);

        // disable all modules
        injector.getInstance(Module.class).disable();

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

    @Override
    public void reload() throws ReloadException {
        if (injector == null) return;

        ReloadException reloadException = null;

        // log plugin reloading
        fLogger.logReloading();

        // close all open inventories
        injector.getInstance(InventoryController.class).closeAll();

        // reload registries
        injector.getInstance(CommandParserRegistry.class).reload();
        injector.getInstance(CommandRegistry.class).reload();
        injector.getInstance(ListenerRegistry.class).reload();
        injector.getInstance(MessageProcessRegistry.class).reload();
        injector.getInstance(PermissionRegistry.class).reload();
        injector.getInstance(ProxyRegistry.class).reload();

        // reload event process registry
        EventProcessRegistry eventProcessRegistry = injector.getInstance(EventProcessRegistry.class);
        eventProcessRegistry.reload();

        // reload task scheduler
        injector.getInstance(TaskScheduler.class).reload();

        // get file resolver for configuration
        FileResolver fileResolver = injector.getInstance(FileResolver.class);

        try {
            // reload configuration files
            fileResolver.reload();
        } catch (Exception e) {
            reloadException = new ReloadException(e.getMessage(), e);
        }

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
        fPlayerService.getPlatformFPlayers().forEach(fPlayer ->
                eventProcessRegistry.processEvent(new PlayerLoadEvent(fPlayer))
        );

        // reload metrics service if enabled
        if (fileResolver.getConfig().isMetrics()) {
            injector.getInstance(MetricsService.class).reload();
        }

        // reload disabled tags
        injector.getInstance(MessagePipeline.class).reload();

        // log plugin reloaded
        fLogger.logReloaded();

        // throw reload exception if occurred
        if (reloadException != null) {
            throw reloadException;
        }
    }
}