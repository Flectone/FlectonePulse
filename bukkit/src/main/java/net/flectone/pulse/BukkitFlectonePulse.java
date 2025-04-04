package net.flectone.pulse;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Setter;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.controller.InventoryController;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.Module;
import net.flectone.pulse.module.integration.discord.DiscordModule;
import net.flectone.pulse.module.integration.telegram.TelegramModule;
import net.flectone.pulse.module.integration.twitch.TwitchModule;
import net.flectone.pulse.module.message.bubble.manager.BubbleManager;
import net.flectone.pulse.module.message.mark.manager.MarkManager;
import net.flectone.pulse.resolver.BukkitLibraryResolver;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.MetricsService;
import net.flectone.pulse.util.logging.FLogger;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.objective.ObjectiveManager;
import net.megavex.scoreboardlibrary.api.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Singleton
public class BukkitFlectonePulse extends JavaPlugin implements FlectonePulse {

    @Setter
    private boolean disableSilently = false;

    private FLogger fLogger;
    private LibraryResolver libraryResolver;
    private Injector injector;

    @Override
    public void onLoad() {
        fLogger = new FLogger(this.getLogger());

        fLogger.enableFilter();
        fLogger.logEnabling();

        libraryResolver = new BukkitLibraryResolver(this, fLogger);

        libraryResolver.addLibraries();
        libraryResolver.resolveRepositories();
        libraryResolver.loadLibraries();

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings()
                .reEncodeByDefault(false)
                .checkForUpdates(false)
                .debug(false);

        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        injector = Guice.createInjector(new BukkitInjector(this, this, libraryResolver, fLogger));

        fLogger.logPluginInfo();

        if (injector == null || disableSilently) {
            fLogger.warning("FAILED TO ENABLE");
            fLogger.warning("Report a problem on github https://github.com/Flectone/FlectonePulse/issues");
            fLogger.warning("or in discord https://discord.com/channels/861147957365964810/1271850075064369152");
            return;
        }

        FileManager fileManager = injector.getInstance(FileManager.class);

        fLogger.reload(fileManager.getConfig().getLogFilter());

        // test
//        PacketEvents.getAPI().getEventManager().registerListener(injector.getInstance(TestListener.class), PacketListenerPriority.NORMAL);

        PacketEvents.getAPI().init();

        injector.getInstance(Module.class).reloadWithChildren();

        try {
            injector.getInstance(Database.class).connect();
        } catch (Exception e) {
            fLogger.warning("Failed to connect database");
            fLogger.warning(e);
            return;
        }

        injector.getInstance(FPlayerService.class).reload();
        injector.getInstance(ProxySender.class).reload();

        if (fileManager.getConfig().isMetrics()) {
            injector.getInstance(MetricsService.class).register();
        }
        
        injector.getInstance(ListenerRegistry.class).registerDefaultListeners();

        fLogger.logEnabled();
    }

    @Override
    public void onDisable() {
        if (injector == null || disableSilently) return;

        fLogger.logDisabling();

        injector.getInstance(InventoryController.class).closeAll();

        FPlayerService fPlayerService = injector.getInstance(FPlayerService.class);

        fPlayerService.getFPlayers().forEach(fPlayer -> {
            fPlayer.setOnline(false);
            fPlayerService.saveOrUpdateFPlayer(fPlayer);
        });

        fPlayerService.clear();

        injector.getInstance(ScoreboardLibrary.class).close();
        injector.getInstance(Database.class).disconnect();
        injector.getInstance(ObjectiveManager.class).close();
        injector.getInstance(TeamManager.class).close();
        injector.getInstance(ListenerRegistry.class).unregisterAll();
        PacketEvents.getAPI().terminate();

        injector.getInstance(ProxySender.class).disable();

        injector.getInstance(DiscordModule.class).disconnect();
        injector.getInstance(TwitchModule.class).disconnect();
        injector.getInstance(TelegramModule.class).disconnect();
        injector.getInstance(TaskScheduler.class).reload();

        fLogger.logDisabled();
    }

    @Override
    public void reload() {
        if (injector == null) return;

        fLogger.logReloading();

        injector.getInstance(InventoryController.class).closeAll();

        injector.getInstance(CommandRegistry.class).reload();
        injector.getInstance(ListenerRegistry.class).reload();
        injector.getInstance(TaskScheduler.class).reload();
        injector.getInstance(BubbleManager.class).reload();
        injector.getInstance(MarkManager.class).reload();

        FileManager fileManager = injector.getInstance(FileManager.class);
        fileManager.reload();

        fLogger.reload(fileManager.getConfig().getLogFilter());

        try {
            injector.getInstance(Database.class).disconnect();
            injector.getInstance(Database.class).connect();

        } catch (Exception e) {
            fLogger.warning("Failed to connect database");

            Bukkit.getPluginManager().disablePlugin(this);
        }

        injector.getInstance(ProxySender.class).reload();
        injector.getInstance(FPlayerService.class).reload();
        injector.getInstance(Module.class).reloadWithChildren();

        fLogger.logReloaded();
    }
}