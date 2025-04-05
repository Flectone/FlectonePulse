package net.flectone.pulse;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import io.github.retrooper.packetevents.adventure.serializer.gson.GsonComponentSerializer;
import net.flectone.pulse.adapter.BukkitPlayerAdapter;
import net.flectone.pulse.adapter.BukkitServerAdapter;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.checker.BukkitPermissionChecker;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.formatter.BukkitItemTextFormatter;
import net.flectone.pulse.formatter.ItemTextFormatter;
import net.flectone.pulse.sender.BukkitProxySender;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.controller.BukkitInventoryController;
import net.flectone.pulse.controller.InventoryController;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.spy.BukkitSpyModule;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.integration.BukkitIntegrationModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.module.message.afk.BukkitAfkModule;
import net.flectone.pulse.module.message.anvil.AnvilModule;
import net.flectone.pulse.module.message.anvil.BukkitAnvilModule;
import net.flectone.pulse.module.message.book.BookModule;
import net.flectone.pulse.module.message.book.BukkitBookModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.bubble.BukkitBubbleModule;
import net.flectone.pulse.module.message.bubble.manager.BubbleManager;
import net.flectone.pulse.module.message.bubble.manager.BukkitBubbleManager;
import net.flectone.pulse.module.message.chat.BukkitChatModule;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.module.message.format.BukkitFormatModule;
import net.flectone.pulse.module.message.format.FormatModule;
import net.flectone.pulse.module.message.format.scoreboard.BukkitScoreboardModule;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.join.BukkitJoinModule;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.objective.BukkitObjectiveModule;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.module.message.quit.BukkitQuitModule;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.module.message.rightclick.BukkitRightclickModule;
import net.flectone.pulse.module.message.rightclick.RightclickModule;
import net.flectone.pulse.module.message.sidebar.BukkitSidebarModule;
import net.flectone.pulse.module.message.sidebar.SidebarModule;
import net.flectone.pulse.module.message.sign.BukkitSignModule;
import net.flectone.pulse.module.message.sign.SignModule;
import net.flectone.pulse.sender.BukkitMessageSender;
import net.flectone.pulse.registry.*;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.sender.MessageSender;
import net.flectone.pulse.scheduler.BukkitTaskScheduler;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.BukkitMetricsService;
import net.flectone.pulse.service.MetricsService;
import net.flectone.pulse.util.*;
import net.flectone.pulse.util.interceptor.AsyncInterceptor;
import net.flectone.pulse.util.interceptor.SyncInterceptor;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException;
import net.megavex.scoreboardlibrary.api.noop.NoopScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.objective.ObjectiveManager;
import net.megavex.scoreboardlibrary.api.team.TeamManager;
import org.bukkit.plugin.Plugin;

import java.io.InputStream;
import java.nio.file.Path;

@Singleton
public class BukkitInjector extends AbstractModule {

    private final BukkitFlectonePulse instance;
    private final Plugin plugin;
    private final LibraryResolver libraryResolver;
    private final FLogger fLogger;

    public BukkitInjector(BukkitFlectonePulse instance,
                          Plugin plugin,
                          LibraryResolver libraryResolver,
                          FLogger fLogger) {
        this.instance = instance;
        this.plugin = plugin;
        this.libraryResolver = libraryResolver;
        this.fLogger = fLogger;
    }

    @Override
    protected void configure() {
        // Bind project path
        Path projectPath = plugin.getDataFolder().toPath();
        bind(Path.class).annotatedWith(Names.named("projectPath")).toInstance(projectPath);

        // Initialize and bind FileManager
        FileManager fileManager;
        try {
            fileManager = new FileManager(projectPath);
            fileManager.reload();
        } catch (Exception e) {
            fLogger.warning(e);
            instance.setDisableSilently(true);
            return;
        }
        bind(FileManager.class).toInstance(fileManager);

        // Adapters
        bind(PlatformPlayerAdapter.class).to(BukkitPlayerAdapter.class);
        bind(PlatformServerAdapter.class).to(net.flectone.pulse.adapter.BukkitServerAdapter.class);

        // Registries
        bind(PermissionRegistry.class).to(BukkitPermissionRegistry.class);
        bind(ListenerRegistry.class).to(BukkitListenerRegistry.class);
        bind(CommandRegistry.class).to(BukkitCommandRegistry.class);

        // Checkers and utilities
        bind(PermissionChecker.class).to(BukkitPermissionChecker.class);
        bind(TaskScheduler.class).to(BukkitTaskScheduler.class);
        bind(InventoryController.class).to(BukkitInventoryController.class);
        bind(ProxySender.class).to(BukkitProxySender.class);
        bind(ItemTextFormatter.class).to(BukkitItemTextFormatter.class);
        bind(MetricsService.class).to(BukkitMetricsService.class);
        bind(FileUtil.class).to(BukkitFileUtil.class);
        bind(BubbleManager.class).to(BukkitBubbleManager.class);
        bind(MessageSender.class).to(BukkitMessageSender.class);

        // Modules
        bindModules();

        // Libraries and serialization
        bind(LibraryResolver.class).toInstance(libraryResolver);
        bind(Gson.class).toInstance(GsonComponentSerializer.gson().serializer());

        // Core bindings
        bind(FlectonePulse.class).toInstance(instance);
        bind(BukkitFlectonePulse.class).toInstance(instance);
        bind(FlectonePulseAPI.class).asEagerSingleton();
        bind(Plugin.class).toInstance(plugin);
        bind(FLogger.class).toInstance(fLogger);

        // Scheduler
        bind(com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler.class)
                .toInstance(UniversalScheduler.getScheduler(plugin));

        // Interceptors
        setupInterceptors();

        // MiniMessage
        bind(MiniMessage.class).toInstance(MiniMessage.builder().tags(TagResolver.builder().build()).build());

        // Scoreboard
        setupScoreboard();

        // Database SQL files
        setupDatabaseBindings(fileManager);

//        try {
//            Package[] packs = Package.getPackages();
//
//            Arrays.stream(packs)
//                    .map(Package::getName)
//                    .filter(string -> string.contains("net.flectone.pulse.library"))
//                    .sorted()
//                    .forEach(fLogger::warning);
//
//        } catch (Exception e) {
//            fLogger.warning(e);
//        }
    }

    private void bindModules() {
        bind(IntegrationModule.class).to(BukkitIntegrationModule.class);
        bind(net.flectone.pulse.module.message.mark.MarkModule.class)
                .to(net.flectone.pulse.module.message.mark.BukkitMarkModule.class);
        bind(FormatModule.class).to(BukkitFormatModule.class);
        bind(ScoreboardModule.class).to(BukkitScoreboardModule.class);
        bind(ObjectiveModule.class).to(BukkitObjectiveModule.class);
        bind(SidebarModule.class).to(BukkitSidebarModule.class);
        bind(AnvilModule.class).to(BukkitAnvilModule.class);
        bind(BookModule.class).to(BukkitBookModule.class);
        bind(AfkModule.class).to(BukkitAfkModule.class);
        bind(BubbleModule.class).to(BukkitBubbleModule.class);
        bind(ChatModule.class).to(BukkitChatModule.class);
        bind(SignModule.class).to(BukkitSignModule.class);
        bind(RightclickModule.class).to(BukkitRightclickModule.class);
        bind(SpyModule.class).to(BukkitSpyModule.class);

        if (!BukkitServerAdapter.IS_PAPER) {
            bind(JoinModule.class).to(BukkitJoinModule.class);
            bind(QuitModule.class).to(BukkitQuitModule.class);
        }
    }

    private void setupInterceptors() {
        SyncInterceptor syncInterceptor = new SyncInterceptor();
        requestInjection(syncInterceptor);

        AsyncInterceptor asyncInterceptor = new AsyncInterceptor();
        requestInjection(asyncInterceptor);

        bindInterceptor(Matchers.any(),
                Matchers.annotatedWith(Sync.class).or(Matchers.annotatedWith(Async.class)),
                asyncInterceptor,
                syncInterceptor
        );
    }

    private void setupScoreboard() {
        ScoreboardLibrary scoreboardLibrary;
        try {
            scoreboardLibrary = ScoreboardLibrary.loadScoreboardLibrary(plugin);
        } catch (NoPacketAdapterAvailableException e) {
            scoreboardLibrary = new NoopScoreboardLibrary();
            fLogger.warning("No scoreboard packet adapter available!");
        }

        bind(ScoreboardLibrary.class).toInstance(scoreboardLibrary);
        bind(ObjectiveManager.class).toInstance(scoreboardLibrary.createObjectiveManager());
        bind(TeamManager.class).toInstance(scoreboardLibrary.createTeamManager());
    }

    private void setupDatabaseBindings(FileManager fileManager) {
        if (fileManager.getConfig().getDatabase().getType() == Config.Database.Type.MYSQL) {
            bind(InputStream.class).annotatedWith(Names.named("SQLFile"))
                    .toInstance(plugin.getResource("sqls/mysql.sql"));
        } else {
            bind(InputStream.class).annotatedWith(Names.named("SQLFile"))
                    .toInstance(plugin.getResource("sqls/sqlite.sql"));
        }
    }
}
