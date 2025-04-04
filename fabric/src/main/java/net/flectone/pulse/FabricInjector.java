package net.flectone.pulse;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import io.github.retrooper.packetevents.adventure.serializer.gson.GsonComponentSerializer;
import net.fabricmc.loader.api.FabricLoader;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.database.mysql.MySQLDatabase;
import net.flectone.pulse.database.sqlite.SQLiteDatabase;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.manager.*;
import net.flectone.pulse.module.command.CommandModule;
import net.flectone.pulse.module.command.FabricCommandModule;
import net.flectone.pulse.module.command.mail.FabricMailModule;
import net.flectone.pulse.module.command.mail.MailModule;
import net.flectone.pulse.module.command.stream.FabricStreamModule;
import net.flectone.pulse.module.command.stream.StreamModule;
import net.flectone.pulse.module.command.symbol.FabricSymbolModule;
import net.flectone.pulse.module.command.symbol.SymbolModule;
import net.flectone.pulse.module.command.tell.FabricTellModule;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.module.integration.FabricIntegrationModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.anvil.AnvilModule;
import net.flectone.pulse.module.message.anvil.FabricAnvilModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.bubble.FabricBubbleModule;
import net.flectone.pulse.module.message.bubble.manager.BubbleManager;
import net.flectone.pulse.module.message.bubble.manager.FabricBubbleManager;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.module.message.chat.FabricChatModule;
import net.flectone.pulse.module.message.contact.ContactModule;
import net.flectone.pulse.module.message.contact.afk.AfkModule;
import net.flectone.pulse.module.message.contact.afk.FabricAfkModule;
import net.flectone.pulse.module.message.contact.afk.FabricContactModule;
import net.flectone.pulse.module.message.format.FabricFormatModule;
import net.flectone.pulse.module.message.format.FormatModule;
import net.flectone.pulse.module.message.format.name.FabricNameModule;
import net.flectone.pulse.module.message.format.name.NameModule;
import net.flectone.pulse.module.message.objective.FabricObjectiveModule;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.module.message.sidebar.FabricScoreboardModule;
import net.flectone.pulse.module.message.sidebar.ScoreboardModule;
import net.flectone.pulse.platform.DependencyResolver;
import net.flectone.pulse.platform.FabricDependencyResolver;
import net.flectone.pulse.util.*;
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
    private final FabricDependencyResolver fabricDependencyResolver;
    private final FLogger fLogger;

    public FabricInjector(String modId,
                          FlectonePulse flectonePulse,
                          MinecraftServer minecraftServer,
                          FabricDependencyResolver fabricDependencyResolver,
                          FLogger fLogger) {
        this.modId = modId;
        this.flectonePulse = flectonePulse;
        this.minecraftServer = minecraftServer;
        this.fabricDependencyResolver = fabricDependencyResolver;
        this.fLogger = fLogger;
    }

    @Override
    protected void configure() {
        bind(ThreadManager.class).to(FabricThreadManager.class);
        bind(FPlayerManager.class).to(FabricFPlayerManager.class);
        bind(ListenerManager.class).to(FabricListenerManager.class);
        bind(InventoryManager.class).to(FabricInventoryManager.class);
        bind(PermissionUtil.class).to(FabricPermissionUtil.class);
        bind(CommandUtil.class).to(FabricCommandUtil.class);
        bind(ProxyManager.class).to(FabricProxyManager.class);
        bind(ItemUtil.class).to(FabricItemUtil.class);
        bind(platformServerAdapter.class).to(FabricplatformServerAdapter.class);
//        bind(MetricsUtil.class).to(BukkitMetricsUtil.class);
        bind(FileUtil.class).to(FabricFileUtil.class);
        bind(BubbleManager.class).to(FabricBubbleManager.class);

        // modules
        bind(IntegrationModule.class).to(FabricIntegrationModule.class);
        bind(ContactModule.class).to(FabricContactModule.class);
        bind(NameModule.class).to(FabricNameModule.class);
        bind(FormatModule.class).to(FabricFormatModule.class);
        bind(ObjectiveModule.class).to(FabricObjectiveModule.class);
        bind(ScoreboardModule.class).to(FabricScoreboardModule.class);
        bind(AnvilModule.class).to(FabricAnvilModule.class);
//        bind(BookModule.class).to(BukkitBookModule.class);
        bind(AfkModule.class).to(FabricAfkModule.class);
        bind(BubbleModule.class).to(FabricBubbleModule.class);
        bind(ChatModule.class).to(FabricChatModule.class);
//        bind(SignModule.class).to(BukkitSignModule.class);
        bind(CommandModule.class).to(FabricCommandModule.class);

        //commands
//        bind(net.flectone.pulse.module.command.afk.AfkModule.class).to(net.flectone.pulse.module.command.afk.BukkitAfkModule.class);
//        bind(BallModule.class).to(BukkitBallModule.class);
//        bind(BanModule.class).to(BukkitBanModule.class);
//        bind(BanlistModule.class).to(BukkitBanlistModule.class);
//        bind(BroadcastModule.class).to(BukkitBroadcastModule.class);
//        bind(ChatcolorModule.class).to(BukkitChatcolorModule.class);
//        bind(ChatsettingModule.class).to(BukkitChatsettingModule.class);
//        bind(ClearchatModule.class).to(BukkitClearchatModule.class);
//        bind(ClearmailModule.class).to(BukkitClearmailModule.class);
//        bind(CoinModule.class).to(BukkitCoinModule.class);
//        bind(DiceModule.class).to(BukkitDiceModule.class);
//        bind(DoModule.class).to(BukkitDoModule.class);
//        bind(FlectonepulseModule.class).to(BukkitFlectonepulseModule.class);
//        bind(GeolocateModule.class).to(BukkitGeolocateModule.class);
//        bind(HelperModule.class).to(BukkitHelperModule.class);
//        bind(IgnoreModule.class).to(BukkitIngoreModule.class);
//        bind(IgnorelistModule.class).to(BukkitIgnorelistModule.class);
//        bind(KickModule.class).to(BukkitKickModule.class);
        bind(MailModule.class).to(FabricMailModule.class);
//        bind(MaintenanceModule.class).to(BukkitMaintenanceModule.class);
//        bind(MarkModule.class).to(BukkitMarkModule.class);
//        bind(MeModule.class).to(BukkitMeModule.class);
//        bind(MuteModule.class).to(BukkitMuteModule.class);
//        bind(MutelistModule.class).to(BukkitMutelistModule.class);
//        bind(OnlineModule.class).to(BukkitOnlineModule.class);
//        bind(PollModule.class).to(BukkitPollModule.class);
//        bind(ReplyModule.class).to(BukkitReplyModule.class);
//        bind(RockpaperscissorsModule.class).to(BukkitRockpaperscissorsModule.class);
//        bind(SpitModule.class).to(BukkitSpitModule.class);
//        bind(SpyModule.class).to(BukkitSpyModule.class);
        bind(StreamModule.class).to(FabricStreamModule.class);
        bind(SymbolModule.class).to(FabricSymbolModule.class);
        bind(TellModule.class).to(FabricTellModule.class);
//        bind(TictactoeModule.class).to(BukkitTictactoeModule.class);
//        bind(TranslatetoModule.class).to(BukkitTranslatetoModule.class);
//        bind(TryModule.class).to(BukkitTryModule.class);
//        bind(UnbanModule.class).to(BukkitUnbanModule.class);
//        bind(UnmuteModule.class).to(BukkitUnmuteModule.class);
//        bind(UnwarnModule.class).to(BukkitUnwarnModule.class);
//        bind(WarnModule.class).to(BukkitWarnModule.class);
//        bind(WarnlistModule.class).to(BukkitWarnlistModule.class);

        bind(DependencyResolver.class).toInstance(fabricDependencyResolver);
        bind(Gson.class).toInstance(GsonComponentSerializer.gson().serializer());

        bind(FlectonePulse.class).toInstance(flectonePulse);
        bind(FlectonePulseAPI.class).asEagerSingleton();
        bind(MinecraftServer.class).toInstance(minecraftServer);
        bind(FLogger.class).toInstance(fLogger);

        InterceptorSync interceptorSync = new InterceptorSync();
        requestInjection(interceptorSync);

        InterceptorAsync interceptorAsync = new InterceptorAsync();
        requestInjection(interceptorAsync);

        bindInterceptor(Matchers.any(),
                Matchers.annotatedWith(Sync.class)
                        .or(Matchers.annotatedWith(Async.class))
                        .or(Matchers.annotatedWith(Sync.class)),
                interceptorAsync,
                interceptorSync
        );

        bind(MiniMessage.class).toInstance(MiniMessage.builder().tags(TagResolver.builder().build()).build());

//        ScoreboardLibrary scoreboardLibrary;
//        try {
//            scoreboardLibrary = ScoreboardLibrary.loadScoreboardLibrary(plugin);
//        } catch (NoPacketAdapterAvailableException e) {
//            scoreboardLibrary = new NoopScoreboardLibrary();
//            fLogger.warning("No scoreboard packet adapter available!");
//        }

//        bind(ScoreboardLibrary.class).toInstance(scoreboardLibrary);
//        bind(ObjectiveManager.class).toInstance(scoreboardLibrary.createObjectiveManager());
//        bind(TeamManager.class).toInstance(scoreboardLibrary.createTeamManager());
//        bind(BukkitFPlayerManager.class).asEagerSingleton();

        Path projectPath = FabricLoader.getInstance().getConfigDir().resolve(modId);

        bind(Path.class).annotatedWith(Names.named("projectPath")).toInstance(projectPath);
//        bind(NamespacedKey.class).annotatedWith(Names.named("flectonepulseSign")).toInstance(new NamespacedKey(plugin, "flectonepulse.sign"));

        FileManager fileManager = new FileManager(projectPath);
        fileManager.reload();

        bind(FileManager.class).toInstance(fileManager);

        if (fileManager.getConfig().getDatabase().getType() == Database.Type.MYSQL) {
            bind(InputStream.class).annotatedWith(Names.named("SQLFile")).toInstance(Thread.currentThread().getContextClassLoader().getResourceAsStream("sqls/mysql_flectonepulse.sql"));
            bind(Database.class).to(MySQLDatabase.class);
        } else {
            bind(InputStream.class).annotatedWith(Names.named("SQLFile")).toInstance(Thread.currentThread().getContextClassLoader().getResourceAsStream("sqls/sqlite_flectonepulse.sql"));
            bind(Database.class).to(SQLiteDatabase.class);
        }

        bind(ModuleManager.class).asEagerSingleton();
    }
}
