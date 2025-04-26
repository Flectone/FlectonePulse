package net.flectone.pulse;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import io.github.retrooper.packetevents.adventure.serializer.gson.GsonComponentSerializer;
import net.fabricmc.loader.api.FabricLoader;
import net.flectone.pulse.adapter.FabricPlayerAdapter;
import net.flectone.pulse.adapter.FabricServerAdapter;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.checker.FabricPermissionChecker;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.converter.LegacyMiniConvertor;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.integration.FabricIntegrationModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.anvil.AnvilModule;
import net.flectone.pulse.module.message.anvil.FabricAnvilModule;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.module.message.chat.FabricChatModule;
import net.flectone.pulse.module.message.format.scoreboard.FabricScoreboardModule;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.objective.FabricObjectiveModule;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.module.message.rightclick.FabricRightclickModule;
import net.flectone.pulse.module.message.rightclick.RightclickModule;
import net.flectone.pulse.module.message.sidebar.FabricSidebarModule;
import net.flectone.pulse.module.message.sidebar.SidebarModule;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.registry.FabricCommandRegistry;
import net.flectone.pulse.registry.FabricPermissionRegistry;
import net.flectone.pulse.registry.PermissionRegistry;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.scheduler.FabricTaskScheduler;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.sender.FabricMessageSender;
import net.flectone.pulse.sender.FabricProxySender;
import net.flectone.pulse.sender.MessageSender;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.util.FabricFileUtil;
import net.flectone.pulse.util.FileUtil;
import net.flectone.pulse.util.interceptor.AsyncInterceptor;
import net.flectone.pulse.util.interceptor.SyncInterceptor;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minecraft.server.MinecraftServer;

import java.io.InputStream;
import java.nio.file.Path;

@Singleton
public class FabricInjector extends AbstractModule {

    private final String modId;
    private final FlectonePulse flectonePulse;
    private final MinecraftServer minecraftServer;
    private final LibraryResolver libraryResolver;
    private final FLogger fLogger;

    public FabricInjector(String modId,
                          FlectonePulse flectonePulse,
                          MinecraftServer minecraftServer,
                          LibraryResolver libraryResolver,
                          FLogger fLogger) {
        this.modId = modId;
        this.flectonePulse = flectonePulse;
        this.minecraftServer = minecraftServer;
        this.libraryResolver = libraryResolver;
        this.fLogger = fLogger;
    }

    @Override
    protected void configure() {
        // Bind project path
        Path projectPath = FabricLoader.getInstance().getConfigDir().resolve(modId);
        bind(Path.class).annotatedWith(Names.named("projectPath")).toInstance(projectPath);

        // Initialize and bind FileManager
        FileManager fileManager = new FileManager(projectPath);
        fileManager.reload();

        bind(FileManager.class).toInstance(fileManager);

        // Adapters
        bind(PlatformPlayerAdapter.class).to(FabricPlayerAdapter.class);
        bind(PlatformServerAdapter.class).to(FabricServerAdapter.class);

        // Registries
        bind(PermissionRegistry.class).to(FabricPermissionRegistry.class);
        bind(CommandRegistry.class).to(FabricCommandRegistry.class);

        // Checkers and utilities
        bind(PermissionChecker.class).to(FabricPermissionChecker.class);
        bind(TaskScheduler.class).to(FabricTaskScheduler.class);
        bind(ProxySender.class).to(FabricProxySender.class);
        bind(FileUtil.class).to(FabricFileUtil.class);
        bind(MessageSender.class).to(FabricMessageSender.class);

        // Modules
        bindModules();

        // Libraries and serialization
        bind(LibraryResolver.class).toInstance(libraryResolver);
        bind(Gson.class).toInstance(GsonComponentSerializer.gson().serializer());
        bind(LegacyMiniConvertor.class).asEagerSingleton();

        // Core bindings
        bind(FlectonePulse.class).toInstance(flectonePulse);
        bind(FlectonePulseAPI.class).asEagerSingleton();
        bind(MinecraftServer.class).toInstance(minecraftServer);
        bind(net.flectone.pulse.util.logging.FLogger.class).toInstance(fLogger);

        // Interceptors
        setupInterceptors();

        // MiniMessage
        bind(MiniMessage.class).toInstance(MiniMessage.builder().tags(TagResolver.builder().build()).build());

        // Scoreboard
        setupScoreboard();

        // Database SQL files
        setupDatabaseBindings(fileManager);
    }

    private void bindModules() {
        bind(IntegrationModule.class).to(FabricIntegrationModule.class);
        bind(ScoreboardModule.class).to(FabricScoreboardModule.class);
        bind(ObjectiveModule.class).to(FabricObjectiveModule.class);
        bind(SidebarModule.class).to(FabricSidebarModule.class);
        bind(AnvilModule.class).to(FabricAnvilModule.class);
//        bind(AfkModule.class).to(BukkitAfkModule.class);
//        bind(BubbleModule.class).to(BukkitBubbleModule.class);
        bind(ChatModule.class).to(FabricChatModule.class);
//        bind(SignModule.class).to(BukkitSignModule.class);
        bind(RightclickModule.class).to(FabricRightclickModule.class);
//        bind(SpyModule.class).to(BukkitSpyModule.class);
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
//        ScoreboardLibrary scoreboardLibrary;
//        try {
//            scoreboardLibrary = ScoreboardLibrary.loadScoreboardLibrary(plugin);
//        } catch (NoPacketAdapterAvailableException e) {
//            scoreboardLibrary = new NoopScoreboardLibrary();
//            fLogger.warning("No scoreboard packet adapter available!");
//        }
//
//        bind(ScoreboardLibrary.class).toInstance(scoreboardLibrary);
//        bind(ObjectiveManager.class).toInstance(scoreboardLibrary.createObjectiveManager());
//        bind(TeamManager.class).toInstance(scoreboardLibrary.createTeamManager());
    }

    private void setupDatabaseBindings(FileManager fileManager) {
        if (fileManager.getConfig().getDatabase().getType() == Config.Database.Type.MYSQL) {
            bind(InputStream.class).annotatedWith(Names.named("SQLFile"))
                    .toInstance(Thread.currentThread().getContextClassLoader().getResourceAsStream("sqls/mysql.sql"));
        } else {
            bind(InputStream.class).annotatedWith(Names.named("SQLFile"))
                    .toInstance(Thread.currentThread().getContextClassLoader().getResourceAsStream("sqls/sqlite.sql"));
        }
    }
}
