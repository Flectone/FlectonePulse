package net.flectone.pulse;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.*;
import net.flectone.pulse.module.integration.discord.DiscordModule;
import net.flectone.pulse.module.integration.telegram.TelegramModule;
import net.flectone.pulse.module.integration.twitch.TwitchModule;
import net.flectone.pulse.module.message.bubble.manager.BubbleManager;
import net.flectone.pulse.module.message.contact.mark.manager.MarkManager;
import net.flectone.pulse.platform.DependencyResolver;
import net.flectone.pulse.platform.BukkitDependencyResolver;
import net.flectone.pulse.util.MetricsUtil;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.objective.ObjectiveManager;
import net.megavex.scoreboardlibrary.api.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Singleton
public class BukkitFlectonePulse extends JavaPlugin implements FlectonePulse {

    private FLogger fLogger;
    private Injector injector;

    @Getter private DependencyResolver dependencyResolver;

    @Override
    public void onLoad() {
        fLogger = new FLogger(this.getLogger(), null);

        fLogger.enableFilter();
        fLogger.logEnabling();

        dependencyResolver = new BukkitDependencyResolver(this, fLogger);

        dependencyResolver.loadLibraries();
        dependencyResolver.resolveDependencies();

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings()
                .reEncodeByDefault(false)
                .checkForUpdates(false)
                .debug(false);

        PacketEvents.getAPI().load();

        CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
                .silentLogs(true));
    }

    @Override
    public void onEnable() {
        try {
            injector = Guice.createInjector(new BukkitInjector(this, this, fLogger));
        } catch (Exception e) {
            fLogger.warning(e);
            Bukkit.getPluginManager().disablePlugin(this);
        }

        fLogger.logPluginInfo();

        // test
//        PacketEvents.getAPI().getEventManager().registerListener(injector.getInstance(TestListener.class), PacketListenerPriority.NORMAL);

        CommandAPI.onEnable();

        PacketEvents.getAPI().init();

        FileManager fileManager = injector.getInstance(FileManager.class);
        fileManager.reload();

        fLogger.reload(fileManager.getConfig().getLogFilter());

        injector.getInstance(ModuleManager.class).reload();

        try {
            injector.getInstance(Database.class).connect();
        } catch (Exception e) {
            fLogger.warning("Failed to connect database");
            fLogger.warning(e);

            Bukkit.getPluginManager().disablePlugin(this);
        }

        injector.getInstance(FPlayerManager.class).reload();
        injector.getInstance(ProxyManager.class).reload();

        if (fileManager.getConfig().isMetrics()) {
            injector.getInstance(MetricsUtil.class).setup();
        }

        injector.getInstance(ListenerManager.class).registerDefaultListeners();

        fLogger.logEnabled();
    }

    @Override
    public void onDisable() {
        fLogger.logDisabling();

        injector.getInstance(InventoryManager.class).closeAll();

        Database database = injector.getInstance(Database.class);

        injector.getInstance(FPlayerManager.class).getFPlayers().forEach(fPlayer -> {
            fPlayer.setOnline(false);
            try {
                database.updateFPlayer(fPlayer);
            } catch (SQLException e) {
                fLogger.warning(e);
            }
        });

        injector.getInstance(ScoreboardLibrary.class).close();
        database.disconnect();
        injector.getInstance(ObjectiveManager.class).close();
        injector.getInstance(TeamManager.class).close();
        injector.getInstance(ListenerManager.class).unregisterAll();
        PacketEvents.getAPI().terminate();

//        injector.getInstance(InventoryManager.class).closeAll();

        injector.getInstance(ProxyManager.class).disable();

        CommandAPI.onDisable();
        FileManager fileManager = injector.getInstance(FileManager.class);
//        configManager.save();
//        injector.getInstance(DatabaseThread.class).close();
        injector.getInstance(DiscordModule.class).disconnect();
        injector.getInstance(TwitchModule.class).disconnect();
        injector.getInstance(TelegramModule.class).disconnect();
        injector.getInstance(ThreadManager.class).reload();

        fLogger.logDisabled();
    }

    @Override
    public void reload() {
        fLogger.logReloading();

        injector.getInstance(InventoryManager.class).closeAll();

        CommandAPI.getRegisteredCommands().stream()
                .filter(registeredCommand -> registeredCommand.shortDescription().isPresent()
                        && registeredCommand.shortDescription().get().equals("flectonepulse"))
                .flatMap(registeredCommand -> Arrays.stream(registeredCommand.aliases()))
                .collect(Collectors.toSet())
                .forEach(CommandAPI::unregister);

        injector.getInstance(ListenerManager.class).reload();
        injector.getInstance(ThreadManager.class).reload();
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

        injector.getInstance(ProxyManager.class).reload();
        injector.getInstance(FPlayerManager.class).reload();
        injector.getInstance(ModuleManager.class).reload();

        fLogger.logReloaded();
    }
}