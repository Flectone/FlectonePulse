package net.flectone.pulse;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import io.github.retrooper.packetevents.adventure.serializer.gson.GsonComponentSerializer;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.database.mysql.MySQLDatabase;
import net.flectone.pulse.database.sqlite.SQLiteDatabase;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.*;
import net.flectone.pulse.module.command.ball.BallModule;
import net.flectone.pulse.module.command.ball.BukkitBallModule;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.module.command.ban.BukkitBanModule;
import net.flectone.pulse.module.command.banlist.BanlistModule;
import net.flectone.pulse.module.command.banlist.BukkitBanlistModule;
import net.flectone.pulse.module.command.broadcast.BroadcastModule;
import net.flectone.pulse.module.command.broadcast.BukkitBroadcastModule;
import net.flectone.pulse.module.command.chatcolor.BukkitChatcolorModule;
import net.flectone.pulse.module.command.chatcolor.ChatcolorModule;
import net.flectone.pulse.module.command.chatsetting.BukkitChatsettingModule;
import net.flectone.pulse.module.command.chatsetting.ChatsettingModule;
import net.flectone.pulse.module.command.clearchat.BukkitClearchatModule;
import net.flectone.pulse.module.command.clearchat.ClearchatModule;
import net.flectone.pulse.module.command.clearmail.BukkitClearmailModule;
import net.flectone.pulse.module.command.clearmail.ClearmailModule;
import net.flectone.pulse.module.command.coin.BukkitCoinModule;
import net.flectone.pulse.module.command.coin.CoinModule;
import net.flectone.pulse.module.command.dice.BukkitDiceModule;
import net.flectone.pulse.module.command.dice.DiceModule;
import net.flectone.pulse.module.command.do_.BukkitDoModule;
import net.flectone.pulse.module.command.do_.DoModule;
import net.flectone.pulse.module.command.flectonepulse.BukkitFlectonepulseModule;
import net.flectone.pulse.module.command.flectonepulse.FlectonepulseModule;
import net.flectone.pulse.module.command.geolocate.BukkitGeolocateModule;
import net.flectone.pulse.module.command.geolocate.GeolocateModule;
import net.flectone.pulse.module.command.helper.BukkitHelperModule;
import net.flectone.pulse.module.command.helper.HelperModule;
import net.flectone.pulse.module.command.ignore.BukkitIngoreModule;
import net.flectone.pulse.module.command.ignore.IgnoreModule;
import net.flectone.pulse.module.command.ignorelist.BukkitIgnorelistModule;
import net.flectone.pulse.module.command.ignorelist.IgnorelistModule;
import net.flectone.pulse.module.command.kick.BukkitKickModule;
import net.flectone.pulse.module.command.kick.KickModule;
import net.flectone.pulse.module.command.mail.BukkitMailModule;
import net.flectone.pulse.module.command.mail.MailModule;
import net.flectone.pulse.module.command.maintenance.BukkitMaintenanceModule;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.module.command.mark.BukkitMarkModule;
import net.flectone.pulse.module.command.mark.MarkModule;
import net.flectone.pulse.module.command.me.BukkitMeModule;
import net.flectone.pulse.module.command.me.MeModule;
import net.flectone.pulse.module.command.mute.BukkitMuteModule;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.module.command.mutelist.BukkitMutelistModule;
import net.flectone.pulse.module.command.mutelist.MutelistModule;
import net.flectone.pulse.module.command.online.BukkitOnlineModule;
import net.flectone.pulse.module.command.online.OnlineModule;
import net.flectone.pulse.module.command.ping.BukkitPingModule;
import net.flectone.pulse.module.command.ping.PingModule;
import net.flectone.pulse.module.command.poll.BukkitPollModule;
import net.flectone.pulse.module.command.poll.PollModule;
import net.flectone.pulse.module.command.reply.BukkitReplyModule;
import net.flectone.pulse.module.command.reply.ReplyModule;
import net.flectone.pulse.module.command.rockpaperscissors.BukkitRockpaperscissorsModule;
import net.flectone.pulse.module.command.rockpaperscissors.RockpaperscissorsModule;
import net.flectone.pulse.module.command.spit.BukkitSpitModule;
import net.flectone.pulse.module.command.spit.SpitModule;
import net.flectone.pulse.module.command.spy.BukkitSpyModule;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.command.stream.BukkitStreamModule;
import net.flectone.pulse.module.command.stream.StreamModule;
import net.flectone.pulse.module.command.symbol.BukkitSymbolModule;
import net.flectone.pulse.module.command.symbol.SymbolModule;
import net.flectone.pulse.module.command.tell.BukkitTellModule;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.module.command.tictactoe.BukkitTictactoeModule;
import net.flectone.pulse.module.command.tictactoe.TictactoeModule;
import net.flectone.pulse.module.command.translateto.BukkitTranslatetoModule;
import net.flectone.pulse.module.command.translateto.TranslatetoModule;
import net.flectone.pulse.module.command.try_.BukkitTryModule;
import net.flectone.pulse.module.command.try_.TryModule;
import net.flectone.pulse.module.command.unban.BukkitUnbanModule;
import net.flectone.pulse.module.command.unban.UnbanModule;
import net.flectone.pulse.module.command.unmute.BukkitUnmuteModule;
import net.flectone.pulse.module.command.unmute.UnmuteModule;
import net.flectone.pulse.module.command.unwarn.BukkitUnwarnModule;
import net.flectone.pulse.module.command.unwarn.UnwarnModule;
import net.flectone.pulse.module.command.warn.BukkitWarnModule;
import net.flectone.pulse.module.command.warn.WarnModule;
import net.flectone.pulse.module.command.warnlist.BukkitWarnlistModule;
import net.flectone.pulse.module.command.warnlist.WarnlistModule;
import net.flectone.pulse.module.integration.BukkitIntegrationModule;
import net.flectone.pulse.module.integration.IntegrationModule;
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
import net.flectone.pulse.module.message.contact.BukkitContactModule;
import net.flectone.pulse.module.message.contact.ContactModule;
import net.flectone.pulse.module.message.contact.afk.AfkModule;
import net.flectone.pulse.module.message.contact.afk.BukkitAfkModule;
import net.flectone.pulse.module.message.format.BukkitFormatModule;
import net.flectone.pulse.module.message.format.FormatModule;
import net.flectone.pulse.module.message.format.name.BukkitNameModule;
import net.flectone.pulse.module.message.format.name.NameModule;
import net.flectone.pulse.module.message.objective.BukkitObjectiveModule;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.module.message.scoreboard.BukkitScoreboardModule;
import net.flectone.pulse.module.message.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.sign.BukkitSignModule;
import net.flectone.pulse.module.message.sign.SignModule;
import net.flectone.pulse.platform.BukkitMessageSender;
import net.flectone.pulse.platform.LibraryResolver;
import net.flectone.pulse.platform.MessageSender;
import net.flectone.pulse.util.*;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException;
import net.megavex.scoreboardlibrary.api.noop.NoopScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.objective.ObjectiveManager;
import net.megavex.scoreboardlibrary.api.team.TeamManager;
import org.bukkit.NamespacedKey;
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
        Path projectPath = plugin.getDataFolder().toPath();

        bind(Path.class).annotatedWith(Names.named("projectPath")).toInstance(projectPath);
        bind(NamespacedKey.class).annotatedWith(Names.named("flectonepulseSign")).toInstance(new NamespacedKey(plugin, "flectonepulse.sign"));

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

        bind(ThreadManager.class).to(BukkitThreadManager.class);
        bind(FPlayerManager.class).to(BukkitFPlayerManager.class);
        bind(ListenerManager.class).to(BukkitListenerManager.class);
        bind(InventoryManager.class).to(BukkitInventoryManager.class);
        bind(PermissionUtil.class).to(BukkitPermissionUtil.class);
        bind(CommandUtil.class).to(BukkitCommandUtil.class);
        bind(ProxyManager.class).to(BukkitProxyManager.class);
        bind(ItemUtil.class).to(BukkitItemUtil.class);
        bind(ServerUtil.class).to(BukkitServerUtil.class);
        bind(MetricsUtil.class).to(BukkitMetricsUtil.class);
        bind(FileUtil.class).to(BukkitFileUtil.class);
        bind(BubbleManager.class).to(BukkitBubbleManager.class);
        bind(MessageSender.class).to(BukkitMessageSender.class);

        // modules
        bind(IntegrationModule.class).to(BukkitIntegrationModule.class);
        bind(ContactModule.class).to(BukkitContactModule.class);
        bind(NameModule.class).to(BukkitNameModule.class);
        bind(FormatModule.class).to(BukkitFormatModule.class);
        bind(ObjectiveModule.class).to(BukkitObjectiveModule.class);
        bind(ScoreboardModule.class).to(BukkitScoreboardModule.class);
        bind(AnvilModule.class).to(BukkitAnvilModule.class);
        bind(BookModule.class).to(BukkitBookModule.class);
        bind(AfkModule.class).to(BukkitAfkModule.class);
        bind(BubbleModule.class).to(BukkitBubbleModule.class);
        bind(ChatModule.class).to(BukkitChatModule.class);
        bind(SignModule.class).to(BukkitSignModule.class);

        //commands
        bind(net.flectone.pulse.module.command.afk.AfkModule.class).to(net.flectone.pulse.module.command.afk.BukkitAfkModule.class);
        bind(BallModule.class).to(BukkitBallModule.class);
        bind(BanModule.class).to(BukkitBanModule.class);
        bind(BanlistModule.class).to(BukkitBanlistModule.class);
        bind(BroadcastModule.class).to(BukkitBroadcastModule.class);
        bind(ChatcolorModule.class).to(BukkitChatcolorModule.class);
        bind(ChatsettingModule.class).to(BukkitChatsettingModule.class);
        bind(ClearchatModule.class).to(BukkitClearchatModule.class);
        bind(ClearmailModule.class).to(BukkitClearmailModule.class);
        bind(CoinModule.class).to(BukkitCoinModule.class);
        bind(DiceModule.class).to(BukkitDiceModule.class);
        bind(DoModule.class).to(BukkitDoModule.class);
        bind(FlectonepulseModule.class).to(BukkitFlectonepulseModule.class);
        bind(GeolocateModule.class).to(BukkitGeolocateModule.class);
        bind(HelperModule.class).to(BukkitHelperModule.class);
        bind(IgnoreModule.class).to(BukkitIngoreModule.class);
        bind(IgnorelistModule.class).to(BukkitIgnorelistModule.class);
        bind(KickModule.class).to(BukkitKickModule.class);
        bind(MailModule.class).to(BukkitMailModule.class);
        bind(MaintenanceModule.class).to(BukkitMaintenanceModule.class);
        bind(MarkModule.class).to(BukkitMarkModule.class);
        bind(MeModule.class).to(BukkitMeModule.class);
        bind(MuteModule.class).to(BukkitMuteModule.class);
        bind(MutelistModule.class).to(BukkitMutelistModule.class);
        bind(OnlineModule.class).to(BukkitOnlineModule.class);
        bind(PingModule.class).to(BukkitPingModule.class);
        bind(PollModule.class).to(BukkitPollModule.class);
        bind(ReplyModule.class).to(BukkitReplyModule.class);
        bind(RockpaperscissorsModule.class).to(BukkitRockpaperscissorsModule.class);
        bind(SpitModule.class).to(BukkitSpitModule.class);
        bind(SpyModule.class).to(BukkitSpyModule.class);
        bind(StreamModule.class).to(BukkitStreamModule.class);
        bind(SymbolModule.class).to(BukkitSymbolModule.class);
        bind(TellModule.class).to(BukkitTellModule.class);
        bind(TictactoeModule.class).to(BukkitTictactoeModule.class);
        bind(TranslatetoModule.class).to(BukkitTranslatetoModule.class);
        bind(TryModule.class).to(BukkitTryModule.class);
        bind(UnbanModule.class).to(BukkitUnbanModule.class);
        bind(UnmuteModule.class).to(BukkitUnmuteModule.class);
        bind(UnwarnModule.class).to(BukkitUnwarnModule.class);
        bind(WarnModule.class).to(BukkitWarnModule.class);
        bind(WarnlistModule.class).to(BukkitWarnlistModule.class);

        bind(LibraryResolver.class).toInstance(libraryResolver);
        bind(Gson.class).toInstance(GsonComponentSerializer.gson().serializer());

        bind(FlectonePulse.class).toInstance(instance);
        bind(BukkitFlectonePulse.class).toInstance(instance);
        bind(FlectonePulseAPI.class).asEagerSingleton();
        bind(Plugin.class).toInstance(plugin);
        bind(FLogger.class).toInstance(fLogger);

        bind(TaskScheduler.class).toInstance(UniversalScheduler.getScheduler(plugin));

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
        bind(BukkitFPlayerManager.class).asEagerSingleton();

        if (fileManager.getConfig().getDatabase().getType() == Database.Type.MYSQL) {
            bind(InputStream.class).annotatedWith(Names.named("SQLFile")).toInstance(plugin.getResource("sqls/mysql_flectonepulse.sql"));
            bind(Database.class).to(MySQLDatabase.class);
        } else {
            bind(InputStream.class).annotatedWith(Names.named("SQLFile")).toInstance(plugin.getResource("sqls/sqlite_flectonepulse.sql"));
            bind(Database.class).to(SQLiteDatabase.class);
        }

        bind(ModuleManager.class).asEagerSingleton();

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
}
