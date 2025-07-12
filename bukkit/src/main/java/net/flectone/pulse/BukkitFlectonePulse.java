package net.flectone.pulse;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Setter;
import net.flectone.pulse.controller.InventoryController;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.module.Module;
import net.flectone.pulse.module.integration.discord.DiscordModule;
import net.flectone.pulse.module.integration.telegram.TelegramModule;
import net.flectone.pulse.module.integration.twitch.TwitchModule;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.BukkitLibraryResolver;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.MetricsService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.logging.FLogger;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.objective.ObjectiveManager;
import net.megavex.scoreboardlibrary.api.team.TeamManager;
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

        try {
            injector.getInstance(Database.class).connect();
        } catch (Exception e) {
            return;
        }

        FileResolver fileResolver = injector.getInstance(FileResolver.class);

        fLogger.reload(fileResolver.getConfig().getLogFilter());

        // test
//        PacketEvents.getAPI().getEventManager().registerListener(injector.getInstance(TestListener.class), PacketListenerPriority.NORMAL);

        PacketEvents.getAPI().init();

        injector.getInstance(Module.class).reloadWithChildren();

        injector.getInstance(FPlayerService.class).reload();
        injector.getInstance(ProxySender.class).reload();

        if (fileResolver.getConfig().isMetrics()) {
            injector.getInstance(MetricsService.class).reload();
        }
        
        injector.getInstance(ListenerRegistry.class).registerDefaultListeners();

        fLogger.logEnabled();
    }

    @Override
    public void onDisable() {
        if (injector == null || disableSilently) return;

        BukkitTaskScheduler taskScheduler = injector.getInstance(BukkitTaskScheduler.class);
        taskScheduler.setDisabled(true);

        fLogger.logDisabling();

        if (injector.getInstance(FileResolver.class).getConfig().isMetrics()) {
            injector.getInstance(MetricsService.class).send();
        }

        injector.getInstance(InventoryController.class).closeAll();

        FPlayerService fPlayerService = injector.getInstance(FPlayerService.class);

        fPlayerService.getFPlayers().forEach(fPlayer -> {
            fPlayer.setOnline(false);
            fPlayerService.updateFPlayer(fPlayer);
        });

        fPlayerService.clear();

        injector.getInstance(ScoreboardLibrary.class).close();
        injector.getInstance(Database.class).disconnect();
        injector.getInstance(ObjectiveManager.class).close();
        injector.getInstance(TeamManager.class).close();
        injector.getInstance(ListenerRegistry.class).unregisterAll();
        PacketEvents.getAPI().terminate();

        injector.getInstance(ProxySender.class).disable();

        // disable all modules
        injector.getInstance(Module.class).disable();

        taskScheduler.reload();

        fLogger.logDisabled();
    }

    @Override
    public void reload() throws Exception {
        if (injector == null) return;

        Exception reloadException = null;

        fLogger.logReloading();

        injector.getInstance(InventoryController.class).closeAll();

        injector.getInstance(CommandRegistry.class).reload();
        injector.getInstance(ListenerRegistry.class).reload();
        injector.getInstance(TaskScheduler.class).reload();

        FileResolver fileResolver = injector.getInstance(FileResolver.class);

        try {
            fileResolver.reload();
        } catch (Exception e) {
            reloadException = e;
        }

        fLogger.reload(fileResolver.getConfig().getLogFilter());

        try {
            injector.getInstance(Database.class).disconnect();
            injector.getInstance(Database.class).connect();
        } catch (Exception e) {
            reloadException = e;
        }

        injector.getInstance(ProxySender.class).reload();
        injector.getInstance(FPlayerService.class).reload();
        injector.getInstance(ModerationService.class).reload();
        injector.getInstance(Module.class).reloadWithChildren();

        if (fileResolver.getConfig().isMetrics()) {
            injector.getInstance(MetricsService.class).reload();
        }

        fLogger.logReloaded();

        if (reloadException != null) {
            throw reloadException;
        }
    }
}