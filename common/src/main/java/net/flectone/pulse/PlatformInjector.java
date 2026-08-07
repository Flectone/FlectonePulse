package net.flectone.pulse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.google.common.cache.Cache;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.SneakyThrows;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.DatabaseImpl;
import net.flectone.pulse.data.repository.CooldownRepository;
import net.flectone.pulse.data.repository.CooldownRepositoryImpl;
import net.flectone.pulse.data.repository.FPlayerRepository;
import net.flectone.pulse.data.repository.FPlayerRepositoryImpl;
import net.flectone.pulse.data.repository.ModerationRepository;
import net.flectone.pulse.data.repository.ModerationRepositoryImpl;
import net.flectone.pulse.data.repository.PlaytimeRepository;
import net.flectone.pulse.data.repository.PlaytimeRepositoryImpl;
import net.flectone.pulse.data.repository.SocialRepository;
import net.flectone.pulse.data.repository.SocialRepositoryImpl;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.dispatcher.EventDispatcherImpl;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.dispatcher.MessageDispatcherImpl;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.pipeline.MessagePipelineImpl;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.execution.scheduler.TaskSchedulerImpl;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.model.util.PlayTime;
import net.flectone.pulse.module.Module;
import net.flectone.pulse.module.ModuleImpl;
import net.flectone.pulse.module.command.CommandModule;
import net.flectone.pulse.module.command.CommandModuleImpl;
import net.flectone.pulse.module.command.afk.AfkModule;
import net.flectone.pulse.module.command.afk.AfkModuleImpl;
import net.flectone.pulse.module.command.anon.AnonModule;
import net.flectone.pulse.module.command.anon.AnonModuleImpl;
import net.flectone.pulse.module.command.ball.BallModule;
import net.flectone.pulse.module.command.ball.BallModuleImpl;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.module.command.ban.BanModuleImpl;
import net.flectone.pulse.module.command.banlist.BanlistModule;
import net.flectone.pulse.module.command.banlist.BanlistModuleImpl;
import net.flectone.pulse.module.command.broadcast.BroadcastModule;
import net.flectone.pulse.module.command.broadcast.BroadcastModuleImpl;
import net.flectone.pulse.module.command.chatcolor.ChatcolorModule;
import net.flectone.pulse.module.command.chatcolor.ChatcolorModuleImpl;
import net.flectone.pulse.module.command.clearchat.ClearchatModule;
import net.flectone.pulse.module.command.clearchat.ClearchatModuleImpl;
import net.flectone.pulse.module.command.clearmail.ClearmailModule;
import net.flectone.pulse.module.command.clearmail.ClearmailModuleImpl;
import net.flectone.pulse.module.command.coin.CoinModule;
import net.flectone.pulse.module.command.coin.CoinModuleImpl;
import net.flectone.pulse.module.command.deletemessage.DeletemessageModule;
import net.flectone.pulse.module.command.deletemessage.DeletemessageModuleImpl;
import net.flectone.pulse.module.command.dice.DiceModule;
import net.flectone.pulse.module.command.dice.DiceModuleImpl;
import net.flectone.pulse.module.command.do_.DoModule;
import net.flectone.pulse.module.command.do_.DoModuleImpl;
import net.flectone.pulse.module.command.emit.EmitModule;
import net.flectone.pulse.module.command.emit.EmitModuleImpl;
import net.flectone.pulse.module.command.flectonepulse.FlectonepulseModule;
import net.flectone.pulse.module.command.flectonepulse.FlectonepulseModuleImpl;
import net.flectone.pulse.module.command.geolocate.GeolocateModule;
import net.flectone.pulse.module.command.geolocate.GeolocateModuleImpl;
import net.flectone.pulse.module.command.helper.HelperModule;
import net.flectone.pulse.module.command.helper.HelperModuleImpl;
import net.flectone.pulse.module.command.ignore.IgnoreModule;
import net.flectone.pulse.module.command.ignore.IgnoreModuleImpl;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.ignorelist.IgnorelistModule;
import net.flectone.pulse.module.command.ignorelist.IgnorelistModuleImpl;
import net.flectone.pulse.module.command.kick.KickModule;
import net.flectone.pulse.module.command.kick.KickModuleImpl;
import net.flectone.pulse.module.command.mail.MailModule;
import net.flectone.pulse.module.command.mail.MailModuleImpl;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.module.command.maintenance.MaintenanceModuleImpl;
import net.flectone.pulse.module.command.me.MeModule;
import net.flectone.pulse.module.command.me.MeModuleImpl;
import net.flectone.pulse.module.command.minesweeper.MinesweeperModule;
import net.flectone.pulse.module.command.minesweeper.MinesweeperModuleImpl;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.module.command.mute.MuteModuleImpl;
import net.flectone.pulse.module.command.mutelist.MutelistModule;
import net.flectone.pulse.module.command.mutelist.MutelistModuleImpl;
import net.flectone.pulse.module.command.nickname.NicknameModule;
import net.flectone.pulse.module.command.nickname.NicknameModuleImpl;
import net.flectone.pulse.module.command.online.OnlineModule;
import net.flectone.pulse.module.command.online.OnlineModuleImpl;
import net.flectone.pulse.module.command.ping.PingModule;
import net.flectone.pulse.module.command.ping.PingModuleImpl;
import net.flectone.pulse.module.command.poll.PollModule;
import net.flectone.pulse.module.command.poll.PollModuleImpl;
import net.flectone.pulse.module.command.reply.ReplyModule;
import net.flectone.pulse.module.command.reply.ReplyModuleImpl;
import net.flectone.pulse.module.command.rockpaperscissors.RockpaperscissorsModule;
import net.flectone.pulse.module.command.rockpaperscissors.RockpaperscissorsModuleImpl;
import net.flectone.pulse.module.command.sprite.SpriteModule;
import net.flectone.pulse.module.command.sprite.SpriteModuleImpl;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.command.spy.SpyModuleImpl;
import net.flectone.pulse.module.command.stream.StreamModule;
import net.flectone.pulse.module.command.stream.StreamModuleImpl;
import net.flectone.pulse.module.command.symbol.SymbolModule;
import net.flectone.pulse.module.command.symbol.SymbolModuleImpl;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.module.command.tell.TellModuleImpl;
import net.flectone.pulse.module.command.tictactoe.TictactoeModule;
import net.flectone.pulse.module.command.tictactoe.TictactoeModuleImpl;
import net.flectone.pulse.module.command.toponline.ToponlineModule;
import net.flectone.pulse.module.command.toponline.ToponlineModuleImpl;
import net.flectone.pulse.module.command.translateto.TranslatetoModule;
import net.flectone.pulse.module.command.translateto.TranslatetoModuleImpl;
import net.flectone.pulse.module.command.try_.TryModule;
import net.flectone.pulse.module.command.try_.TryModuleImpl;
import net.flectone.pulse.module.command.unban.UnbanModule;
import net.flectone.pulse.module.command.unban.UnbanModuleImpl;
import net.flectone.pulse.module.command.unmute.UnmuteModule;
import net.flectone.pulse.module.command.unmute.UnmuteModuleImpl;
import net.flectone.pulse.module.command.unwarn.UnwarnModule;
import net.flectone.pulse.module.command.unwarn.UnwarnModuleImpl;
import net.flectone.pulse.module.command.warn.WarnModule;
import net.flectone.pulse.module.command.warn.WarnModuleImpl;
import net.flectone.pulse.module.command.warnlist.WarnlistModule;
import net.flectone.pulse.module.command.warnlist.WarnlistModuleImpl;
import net.flectone.pulse.module.command.whitelist.WhitelistModule;
import net.flectone.pulse.module.command.whitelist.WhitelistModuleImpl;
import net.flectone.pulse.module.command.whois.WhoisModule;
import net.flectone.pulse.module.command.whois.WhoisModuleImpl;
import net.flectone.pulse.module.integration.deepl.DeeplModule;
import net.flectone.pulse.module.integration.deepl.DeeplModuleImpl;
import net.flectone.pulse.module.integration.discord.DiscordModule;
import net.flectone.pulse.module.integration.discord.DiscordModuleImpl;
import net.flectone.pulse.module.integration.icu.ICUModule;
import net.flectone.pulse.module.integration.icu.ICUModuleImpl;
import net.flectone.pulse.module.integration.luckperms.LuckPermsModule;
import net.flectone.pulse.module.integration.luckperms.LuckPermsModuleImpl;
import net.flectone.pulse.module.integration.telegram.TelegramModule;
import net.flectone.pulse.module.integration.telegram.TelegramModuleImpl;
import net.flectone.pulse.module.integration.twitch.TwitchModule;
import net.flectone.pulse.module.integration.twitch.TwitchModuleImpl;
import net.flectone.pulse.module.integration.yandex.YandexModule;
import net.flectone.pulse.module.integration.yandex.YandexModuleImpl;
import net.flectone.pulse.module.message.MessageModule;
import net.flectone.pulse.module.message.MessageModuleImpl;
import net.flectone.pulse.module.message.anvil.AnvilModule;
import net.flectone.pulse.module.message.anvil.AnvilModuleImpl;
import net.flectone.pulse.module.message.auto.AutoModule;
import net.flectone.pulse.module.message.auto.AutoModuleImpl;
import net.flectone.pulse.module.message.book.BookModule;
import net.flectone.pulse.module.message.book.BookModuleImpl;
import net.flectone.pulse.module.message.bossbar.BossbarModule;
import net.flectone.pulse.module.message.bossbar.BossbarModuleImpl;
import net.flectone.pulse.module.message.brand.BrandModule;
import net.flectone.pulse.module.message.brand.BrandModuleImpl;
import net.flectone.pulse.module.message.format.FormatModule;
import net.flectone.pulse.module.message.format.FormatModuleImpl;
import net.flectone.pulse.module.message.format.animation.AnimationModule;
import net.flectone.pulse.module.message.format.animation.AnimationModuleImpl;
import net.flectone.pulse.module.message.format.condition.ConditionModule;
import net.flectone.pulse.module.message.format.condition.ConditionModuleImpl;
import net.flectone.pulse.module.message.format.fading.FadingModule;
import net.flectone.pulse.module.message.format.fading.FadingModuleImpl;
import net.flectone.pulse.module.message.format.fcolor.FColorModule;
import net.flectone.pulse.module.message.format.fcolor.FColorModuleImpl;
import net.flectone.pulse.module.message.format.fixation.FixationModule;
import net.flectone.pulse.module.message.format.fixation.FixationModuleImpl;
import net.flectone.pulse.module.message.format.mention.MentionModule;
import net.flectone.pulse.module.message.format.mention.MentionModuleImpl;
import net.flectone.pulse.module.message.format.moderation.ModerationModule;
import net.flectone.pulse.module.message.format.moderation.ModerationModuleImpl;
import net.flectone.pulse.module.message.format.moderation.caps.CapsModule;
import net.flectone.pulse.module.message.format.moderation.caps.CapsModuleImpl;
import net.flectone.pulse.module.message.format.moderation.delete.DeleteModule;
import net.flectone.pulse.module.message.format.moderation.delete.DeleteModuleImpl;
import net.flectone.pulse.module.message.format.moderation.flood.FloodModule;
import net.flectone.pulse.module.message.format.moderation.flood.FloodModuleImpl;
import net.flectone.pulse.module.message.format.moderation.newbie.NewbieModule;
import net.flectone.pulse.module.message.format.moderation.newbie.NewbieModuleImpl;
import net.flectone.pulse.module.message.format.moderation.swear.SwearModule;
import net.flectone.pulse.module.message.format.moderation.swear.SwearModuleImpl;
import net.flectone.pulse.module.message.format.names.NamesModule;
import net.flectone.pulse.module.message.format.names.NamesModuleImpl;
import net.flectone.pulse.module.message.format.object.ObjectModule;
import net.flectone.pulse.module.message.format.object.ObjectModuleImpl;
import net.flectone.pulse.module.message.format.padding.PaddingModule;
import net.flectone.pulse.module.message.format.padding.PaddingModuleImpl;
import net.flectone.pulse.module.message.format.questionanswer.QuestionAnswerModule;
import net.flectone.pulse.module.message.format.questionanswer.QuestionAnswerModuleImpl;
import net.flectone.pulse.module.message.format.replacement.ReplacementModule;
import net.flectone.pulse.module.message.format.replacement.ReplacementModuleImpl;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.module.message.format.translate.TranslateModuleImpl;
import net.flectone.pulse.module.message.greeting.GreetingModule;
import net.flectone.pulse.module.message.greeting.GreetingModuleImpl;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.join.JoinModuleImpl;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.module.message.quit.QuitModuleImpl;
import net.flectone.pulse.module.message.rightclick.RightclickModule;
import net.flectone.pulse.module.message.rightclick.RightclickModuleImpl;
import net.flectone.pulse.module.message.scoreboard.objective.ObjectiveModule;
import net.flectone.pulse.module.message.scoreboard.objective.ObjectiveModuleImpl;
import net.flectone.pulse.module.message.sign.SignModule;
import net.flectone.pulse.module.message.sign.SignModuleImpl;
import net.flectone.pulse.module.message.status.StatusModule;
import net.flectone.pulse.module.message.status.StatusModuleImpl;
import net.flectone.pulse.module.message.tab.TabModule;
import net.flectone.pulse.module.message.tab.TabModuleImpl;
import net.flectone.pulse.module.message.update.UpdateModule;
import net.flectone.pulse.module.message.update.UpdateModuleImpl;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleCommandControllerImpl;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.controller.ModuleControllerImpl;
import net.flectone.pulse.platform.filter.RangeFilter;
import net.flectone.pulse.platform.filter.RangeFilterImpl;
import net.flectone.pulse.platform.formatter.IntegrationFormatter;
import net.flectone.pulse.platform.formatter.IntegrationFormatterImpl;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatterImpl;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.formatter.TimeFormatterImpl;
import net.flectone.pulse.platform.formatter.UrlFormatter;
import net.flectone.pulse.platform.formatter.UrlFormatterImpl;
import net.flectone.pulse.platform.handler.CommandExceptionHandler;
import net.flectone.pulse.platform.handler.CommandExceptionHandlerImpl;
import net.flectone.pulse.platform.registry.CacheRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.regitry.CacheRegistryImpl;
import net.flectone.pulse.platform.regitry.ProxyRegistryImpl;
import net.flectone.pulse.platform.sender.CooldownSender;
import net.flectone.pulse.platform.sender.CooldownSenderImpl;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.DisableSenderImpl;
import net.flectone.pulse.platform.sender.IgnoreSender;
import net.flectone.pulse.platform.sender.IgnoreSenderImpl;
import net.flectone.pulse.platform.sender.MetricsSender;
import net.flectone.pulse.platform.sender.MetricsSenderImpl;
import net.flectone.pulse.platform.sender.ModerationListSender;
import net.flectone.pulse.platform.sender.ModerationListSenderImpl;
import net.flectone.pulse.platform.sender.MuteSender;
import net.flectone.pulse.platform.sender.MuteSenderImpl;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.platform.sender.ProxySenderImpl;
import net.flectone.pulse.processing.converter.*;
import net.flectone.pulse.processing.parser.integer.ColorParser;
import net.flectone.pulse.processing.parser.integer.ColorParserImpl;
import net.flectone.pulse.processing.parser.integer.DurationReasonParser;
import net.flectone.pulse.processing.parser.integer.DurationReasonParserImpl;
import net.flectone.pulse.processing.parser.moderation.*;
import net.flectone.pulse.processing.parser.player.*;
import net.flectone.pulse.processing.parser.string.*;
import net.flectone.pulse.processing.processor.PlayerPreLoginProcessor;
import net.flectone.pulse.processing.processor.PlayerPreLoginProcessorImpl;
import net.flectone.pulse.processing.processor.ProxyMessageProcessor;
import net.flectone.pulse.processing.processor.ProxyMessageProcessorImpl;
import net.flectone.pulse.processing.resolver.*;
import net.flectone.pulse.service.*;
import net.flectone.pulse.util.WebUtil;
import net.flectone.pulse.util.WebUtilImpl;
import net.flectone.pulse.util.checker.*;
import net.flectone.pulse.util.comparator.VersionComparator;
import net.flectone.pulse.util.comparator.VersionComparatorImpl;
import net.flectone.pulse.util.constant.CacheName;
import net.flectone.pulse.util.creator.BackupCreator;
import net.flectone.pulse.util.creator.BackupCreatorImpl;
import net.flectone.pulse.util.decorator.ComponentDecorator;
import net.flectone.pulse.util.decorator.ComponentDecoratorImpl;
import net.flectone.pulse.util.file.*;
import net.flectone.pulse.util.generator.RandomGenerator;
import net.flectone.pulse.util.generator.RandomGeneratorImpl;
import net.flectone.pulse.util.logging.FLogger;
import net.flectone.pulse.util.logging.filter.LogFilter;
import net.flectone.pulse.util.logging.filter.LogFilterImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.snakeyaml.engine.v2.api.LoadSettings;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.*;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.yaml.YAMLFactory;
import tools.jackson.dataformat.yaml.YAMLMapper;
import tools.jackson.dataformat.yaml.YAMLWriteFeature;

public abstract class PlatformInjector extends AbstractModule {

    private final Path projectPath;
    private final LibraryResolver libraryResolver;
    private final FLogger fLogger;

    protected PlatformInjector(Path projectPath, LibraryResolver libraryResolver, FLogger fLogger) {
        this.projectPath = projectPath;
        this.libraryResolver = libraryResolver;
        this.fLogger = fLogger;
    }

    @SneakyThrows
    @Override
    protected void configure() {
        bind(FLogger.class).toInstance(fLogger);
        bind(FlectonePulseAPI.class).to(FlectonePulseAPIImpl.class);
        bind(FlectonePulseAPIImpl.class).asEagerSingleton();
        bind(LibraryResolver.class).toInstance(libraryResolver);
        bind(MiniMessage.class).toInstance(MiniMessage.builder().tags(TagResolver.builder().build()).build());
        bind(HttpClient.class).toInstance(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());

        ReflectionResolver reflectionResolver = new ReflectionResolverImpl(libraryResolver);
        bind(ReflectionResolver.class).toInstance(reflectionResolver);

        // bind paths
        bind(Path.class).annotatedWith(Names.named("projectPath")).toInstance(projectPath);
        bind(Path.class).annotatedWith(Names.named("imagePath")).toInstance(projectPath.resolve("images"));
        bind(Path.class).annotatedWith(Names.named("backupPath")).toInstance(projectPath.resolve("backups"));
        bind(Path.class).annotatedWith(Names.named("minecraftPath")).toInstance(projectPath.resolve("minecraft"));

        // create jackson mapper
        bind(ObjectMapper.class).toInstance(createMapper());
        bind(ObjectMapper.class).annotatedWith(Names.named("defaultMapper")).toInstance(new ObjectMapper());

        // bind date format
        bind(SimpleDateFormat.class).toInstance(new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss"));

        // default module bindings — platforms that override a module bind it themselves in setupPlatform()
        bind(Module.class).to(ModuleImpl.class);
        bind(CommandModule.class).to(CommandModuleImpl.class);
        bind(AfkModule.class).to(AfkModuleImpl.class);
        bind(AnonModule.class).to(AnonModuleImpl.class);
        bind(BallModule.class).to(BallModuleImpl.class);
        bind(BanModule.class).to(BanModuleImpl.class);
        bind(BanlistModule.class).to(BanlistModuleImpl.class);
        bind(BroadcastModule.class).to(BroadcastModuleImpl.class);
        bind(ChatcolorModule.class).to(ChatcolorModuleImpl.class);
        bind(ClearchatModule.class).to(ClearchatModuleImpl.class);
        bind(ClearmailModule.class).to(ClearmailModuleImpl.class);
        bind(CoinModule.class).to(CoinModuleImpl.class);
        bind(DeletemessageModule.class).to(DeletemessageModuleImpl.class);
        bind(DiceModule.class).to(DiceModuleImpl.class);
        bind(DoModule.class).to(DoModuleImpl.class);
        bind(EmitModule.class).to(EmitModuleImpl.class);
        bind(FlectonepulseModule.class).to(FlectonepulseModuleImpl.class);
        bind(GeolocateModule.class).to(GeolocateModuleImpl.class);
        bind(HelperModule.class).to(HelperModuleImpl.class);
        bind(IgnoreModule.class).to(IgnoreModuleImpl.class);
        bind(IgnorelistModule.class).to(IgnorelistModuleImpl.class);
        bind(KickModule.class).to(KickModuleImpl.class);
        bind(MailModule.class).to(MailModuleImpl.class);
        bind(MeModule.class).to(MeModuleImpl.class);
        bind(MinesweeperModule.class).to(MinesweeperModuleImpl.class);
        bind(MuteModule.class).to(MuteModuleImpl.class);
        bind(MutelistModule.class).to(MutelistModuleImpl.class);
        bind(NicknameModule.class).to(NicknameModuleImpl.class);
        bind(OnlineModule.class).to(OnlineModuleImpl.class);
        bind(PingModule.class).to(PingModuleImpl.class);
        bind(ReplyModule.class).to(ReplyModuleImpl.class);
        bind(RockpaperscissorsModule.class).to(RockpaperscissorsModuleImpl.class);
        bind(SpriteModule.class).to(SpriteModuleImpl.class);
        bind(StreamModule.class).to(StreamModuleImpl.class);
        bind(SymbolModule.class).to(SymbolModuleImpl.class);
        bind(TellModule.class).to(TellModuleImpl.class);
        bind(TictactoeModule.class).to(TictactoeModuleImpl.class);
        bind(ToponlineModule.class).to(ToponlineModuleImpl.class);
        bind(TranslatetoModule.class).to(TranslatetoModuleImpl.class);
        bind(TryModule.class).to(TryModuleImpl.class);
        bind(UnbanModule.class).to(UnbanModuleImpl.class);
        bind(UnmuteModule.class).to(UnmuteModuleImpl.class);
        bind(UnwarnModule.class).to(UnwarnModuleImpl.class);
        bind(WarnModule.class).to(WarnModuleImpl.class);
        bind(WarnlistModule.class).to(WarnlistModuleImpl.class);
        bind(WhitelistModule.class).to(WhitelistModuleImpl.class);
        bind(WhoisModule.class).to(WhoisModuleImpl.class);
        bind(DeeplModule.class).to(DeeplModuleImpl.class);
        bind(DiscordModule.class).to(DiscordModuleImpl.class);
        bind(ICUModule.class).to(ICUModuleImpl.class);
        bind(LuckPermsModule.class).to(LuckPermsModuleImpl.class);
        bind(TelegramModule.class).to(TelegramModuleImpl.class);
        bind(TwitchModule.class).to(TwitchModuleImpl.class);
        bind(YandexModule.class).to(YandexModuleImpl.class);
        bind(AutoModule.class).to(AutoModuleImpl.class);
        bind(BrandModule.class).to(BrandModuleImpl.class);
        bind(FormatModule.class).to(FormatModuleImpl.class);
        bind(AnimationModule.class).to(AnimationModuleImpl.class);
        bind(ConditionModule.class).to(ConditionModuleImpl.class);
        bind(FadingModule.class).to(FadingModuleImpl.class);
        bind(FColorModule.class).to(FColorModuleImpl.class);
        bind(FixationModule.class).to(FixationModuleImpl.class);
        bind(MentionModule.class).to(MentionModuleImpl.class);
        bind(ModerationModule.class).to(ModerationModuleImpl.class);
        bind(CapsModule.class).to(CapsModuleImpl.class);
        bind(DeleteModule.class).to(DeleteModuleImpl.class);
        bind(FloodModule.class).to(FloodModuleImpl.class);
        bind(NewbieModule.class).to(NewbieModuleImpl.class);
        bind(SwearModule.class).to(SwearModuleImpl.class);
        bind(NamesModule.class).to(NamesModuleImpl.class);
        bind(PaddingModule.class).to(PaddingModuleImpl.class);
        bind(QuestionAnswerModule.class).to(QuestionAnswerModuleImpl.class);
        bind(ReplacementModule.class).to(ReplacementModuleImpl.class);
        bind(TranslateModule.class).to(TranslateModuleImpl.class);
        bind(GreetingModule.class).to(GreetingModuleImpl.class);
        bind(UpdateModule.class).to(UpdateModuleImpl.class);

        // default processing/service/util bindings — platforms that override one bind it themselves in setupPlatform()
        bind(ColorConverter.class).to(ColorConverterImpl.class);
        bind(IconConverter.class).to(IconConverterImpl.class);
        bind(ImagePixelConverter.class).to(ImagePixelConverterImpl.class);
        bind(ColorParser.class).to(ColorParserImpl.class);
        bind(DurationReasonParser.class).to(DurationReasonParserImpl.class);
        bind(BanModerationParser.class).to(BanModerationParserImpl.class);
        bind(MuteModerationParser.class).to(MuteModerationParserImpl.class);
        bind(WarnModerationParser.class).to(WarnModerationParserImpl.class);
        bind(WhitelistModerationParser.class).to(WhitelistModerationParserImpl.class);
        bind(OfflinePlayerParser.class).to(OfflinePlayerParserImpl.class);
        bind(PlatformPlayerParser.class).to(PlatformPlayerParserImpl.class);
        bind(PlayerParser.class).to(PlayerParserImpl.class);
        bind(MessageParser.class).to(MessageParserImpl.class);
        bind(SingleMessageParser.class).to(SingleMessageParserImpl.class);
        bind(URLParser.class).to(URLParserImpl.class);
        bind(UUIDParser.class).to(UUIDParserImpl.class);
        bind(PlayerPreLoginProcessor.class).to(PlayerPreLoginProcessorImpl.class);
        bind(ProxyMessageProcessor.class).to(ProxyMessageProcessorImpl.class);
        bind(SystemVariableResolver.class).to(SystemVariableResolverImpl.class);
        bind(FPlayerService.class).to(FPlayerServiceImpl.class);
        bind(MetricsService.class).to(MetricsServiceImpl.class);
        bind(ModerationService.class).to(ModerationServiceImpl.class);
        bind(PlaytimeService.class).to(PlaytimeServiceImpl.class);
        bind(SocialService.class).to(SocialServiceImpl.class);
        bind(WebUtil.class).to(WebUtilImpl.class);
        bind(CooldownChecker.class).to(CooldownCheckerImpl.class);
        bind(MuteChecker.class).to(MuteCheckerImpl.class);
        bind(ValidNameChecker.class).to(ValidNameCheckerImpl.class);
        bind(VersionComparator.class).to(VersionComparatorImpl.class);
        bind(BackupCreator.class).to(BackupCreatorImpl.class);
        bind(ComponentDecorator.class).to(ComponentDecoratorImpl.class);
        bind(FileFacade.class).to(FileFacadeImpl.class);
        bind(FileLoader.class).to(FileLoaderImpl.class);
        bind(FileMigrator.class).to(FileMigratorImpl.class);
        bind(FilePathProvider.class).to(FilePathProviderImpl.class);
        bind(FileWriter.class).to(FileWriterImpl.class);
        bind(RandomGenerator.class).to(RandomGeneratorImpl.class);
        bind(LogFilter.class).to(LogFilterImpl.class);

        // repositories
        bind(CooldownRepository.class).to(CooldownRepositoryImpl.class);
        bind(FPlayerRepository.class).to(FPlayerRepositoryImpl.class);
        bind(ModerationRepository.class).to(ModerationRepositoryImpl.class);
        bind(PlaytimeRepository.class).to(PlaytimeRepositoryImpl.class);
        bind(SocialRepository.class).to(SocialRepositoryImpl.class);
        bind(Database.class).to(DatabaseImpl.class);

        // dispatchers
        bind(EventDispatcher.class).to(EventDispatcherImpl.class);
        bind(MessageDispatcher.class).to(MessageDispatcherImpl.class);

        // senders
        bind(CooldownSender.class).to(CooldownSenderImpl.class);
        bind(DisableSender.class).to(DisableSenderImpl.class);
        bind(IgnoreSender.class).to(IgnoreSenderImpl.class);
        bind(MetricsSender.class).to(MetricsSenderImpl.class);
        bind(ModerationListSender.class).to(ModerationListSenderImpl.class);
        bind(MuteSender.class).to(MuteSenderImpl.class);
        bind(ProxySender.class).to(ProxySenderImpl.class);

        // formatters
        bind(IntegrationFormatter.class).to(IntegrationFormatterImpl.class);
        bind(ModerationMessageFormatter.class).to(ModerationMessageFormatterImpl.class);
        bind(TimeFormatter.class).to(TimeFormatterImpl.class);
        bind(UrlFormatter.class).to(UrlFormatterImpl.class);

        // controllers, registries and other platform-independent services
        bind(ModuleController.class).to(ModuleControllerImpl.class);
        bind(ModuleCommandController.class).to(ModuleCommandControllerImpl.class);
        bind(CommandExceptionHandler.class).to(CommandExceptionHandlerImpl.class);
        bind(CacheRegistry.class).to(CacheRegistryImpl.class);
        bind(RangeFilter.class).to(RangeFilterImpl.class);

        // Defaults that some platform replaces. Every interface is pointed at its common Impl exactly
        // once, here; a platform that needs different behaviour re-points the Impl in setupPlatform()
        // (bind(FooImpl.class).to(PlatformFoo.class)) instead of re-binding the interface. Guice follows
        // the whole chain, so platforms that are happy with the default simply say nothing.
        bind(MessagePipeline.class).to(MessagePipelineImpl.class);
        bind(TaskScheduler.class).to(TaskSchedulerImpl.class);
        bind(ProxyRegistry.class).to(ProxyRegistryImpl.class);
        bind(MaintenanceModule.class).to(MaintenanceModuleImpl.class);
        bind(PollModule.class).to(PollModuleImpl.class);
        bind(SpyModule.class).to(SpyModuleImpl.class);
        bind(MessageModule.class).to(MessageModuleImpl.class);
        bind(BossbarModule.class).to(BossbarModuleImpl.class);
        bind(JoinModule.class).to(JoinModuleImpl.class);
        bind(ObjectModule.class).to(ObjectModuleImpl.class);
        bind(ObjectiveModule.class).to(ObjectiveModuleImpl.class);
        bind(QuitModule.class).to(QuitModuleImpl.class);
        bind(RightclickModule.class).to(RightclickModuleImpl.class);
        bind(StatusModule.class).to(StatusModuleImpl.class);
        bind(TabModule.class).to(TabModuleImpl.class);
        bind(AnvilModule.class).to(AnvilModuleImpl.class);
        bind(BookModule.class).to(BookModuleImpl.class);
        bind(SignModule.class).to(SignModuleImpl.class);
        // command.afk.AfkModule is bound above; this is the message-side module of the same name
        bind(net.flectone.pulse.module.message.afk.AfkModule.class)
                .to(net.flectone.pulse.module.message.afk.AfkModuleImpl.class);

        // platform binding
        setupPlatform(reflectionResolver);

//        try {
//            Package[] packs = Package.getPackages();
//
//            Arrays.stream(packs)
//                    .map(Package::getName)
//                    .filter(string -> string.contains(BuildConfig.RELOCATED_PATTERN + ""))
//                    .sorted()
//                    .forEach(fLogger::warning);
//
//        } catch (Exception _) {
//            fLogger.warning(e);
//        }
    }

    public abstract void setupPlatform(ReflectionResolver reflectionResolver);

    @Provides
    @Singleton
    @Named("animation")
    public Cache<AnimationModule.AnimationKey, Integer> provideAnimationCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.ANIMATION);
    }

    @Provides
    @Singleton
    @Named("cooldown")
    public Cache<CooldownRepository.CooldownKey, Long> provideCooldownCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.COOLDOWN);
    }

    @Provides
    @Singleton
    @Named("offlinePlayers")
    public Cache<UUID, FPlayer> provideOfflinePlayersCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.OFFLINE_PLAYERS);
    }

    @Provides
    @Singleton
    @Named("profileProperty")
    public Cache<UUID, PlayerHeadObjectContents.ProfileProperty> provideProfilePropertyCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.PROFILE_PROPERTY);
    }

    @Provides
    @Singleton
    @Named("playtime")
    public Cache<UUID, PlayTime> providePlaytimeCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.PLAYTIME);
    }

    @Provides
    @Singleton
    @Named("dialogClick")
    public Cache<UUID, AtomicInteger> provideDialogClickCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.DIALOG_CLICK);
    }

    @Provides
    @Singleton
    @Named("messageContext")
    public Cache<MessageContext.CacheKey, Component> provideMessageContextCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.MESSAGE_CONTEXT);
    }

    @Provides
    @Singleton
    @Named("moderation")
    public Cache<UUID, Map<String, List<Moderation>>> provideModerationCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.MODERATION);
    }

    @Provides
    @Singleton
    @Named("icuMessage")
    public Cache<String, String> provideIcuMessageCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.ICU_MESSAGE);
    }

    @Provides
    @Singleton
    @Named("imagePixels")
    public Cache<String, List<String>> provideImagePixelsCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.IMAGE_PIXELS);
    }

    @Provides
    @Singleton
    @Named("legacyColorMessage")
    public Cache<String, String> provideLegacyColorMessageCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.LEGACY_COLOR_MESSAGE);
    }

    @Provides
    @Singleton
    @Named("mentionMessage")
    public Cache<String, String> provideMentionMessageCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.MENTION_MESSAGE);
    }

    @Provides
    @Singleton
    @Named("swearMessage")
    public Cache<String, String> provideSwearMessageCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.SWEAR_MESSAGE);
    }

    @Provides
    @Singleton
    @Named("replacementMessage")
    public Cache<String, String> provideReplacementMessageCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.REPLACEMENT_MESSAGE);
    }

    @Provides
    @Singleton
    @Named("replacementImage")
    public Cache<String, Component> provideReplacementImageCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.REPLACEMENT_IMAGE);
    }

    @Provides
    @Singleton
    @Named("translateMessage")
    public Cache<String, UUID> provideTranslateMessageCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.TRANSLATE_MESSAGE);
    }

    @Provides
    @Singleton
    @Named("playerColor")
    public Cache<UUID, Map<FColor.Type, Set<FColor>>> providePlayerColorCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.PLAYER_COLOR);
    }

    @Provides
    @Singleton
    @Named("playerSetting")
    public Cache<UUID, SocialRepository.Settings> providePlayerSettingCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.PLAYER_SETTING);
    }

    @Provides
    @Singleton
    @Named("playerIgnore")
    public Cache<UUID, List<Ignore>> providePlayerIgnoreCache(CacheRegistryImpl cacheRegistry) {
        return cacheRegistry.getCache(CacheName.PLAYER_IGNORE);
    }

    private ObjectMapper createMapper() {
        return YAMLMapper.builder(
                        YAMLFactory.builder()
                                .loadSettings(LoadSettings.builder()
                                        .setBufferSize(8192) // increase string limit
                                        .setAllowDuplicateKeys(true) // fix duplicate keys
                                        .build()
                                )
                                .build()
                )
                // mapper
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY) // disable auto sorting
                .disable(MapperFeature.DETECT_PARAMETER_NAMES) // [databind#5314]
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS) // fix enum names
                .enable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS) // fix custom classes deserialization
                // deserialization
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES) // jackson 2.x value
                .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS) // jackson 2.x value
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY) // convert single value to array
                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT) // fix empty null string
                // serialization
                .enable(SerializationFeature.INDENT_OUTPUT) // indent output for values
                .disable(YAMLWriteFeature.SPLIT_LINES) // fix split long values
                .disable(YAMLWriteFeature.WRITE_DOC_START_MARKER) // fix header
                .disable(YAMLWriteFeature.USE_NATIVE_TYPE_ID) // fix type id like !!java.util.Hashmap
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                // enum
                .disable(EnumFeature.READ_ENUMS_USING_TO_STRING) // jackson 2.x value
                .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING) // jackson 2.x value
                // fix nulls
                .changeDefaultPropertyInclusion(_ -> JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL)) // show only non-null values
                .changeDefaultNullHandling(_ -> JsonSetter.Value.forValueNulls(Nulls.SKIP)) // skip null values deserialization
                .withConfigOverride(String.class, o -> o.setNullHandling(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY))) // fix null string
                .withConfigOverride(Collection.class, o -> o.setNullHandling(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY))) // fix null collection
                .withConfigOverride(List.class, o -> o.setNullHandling(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY))) // fix null list
                .withConfigOverride(Set.class, o -> o.setNullHandling(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY))) // fix null set
                .withConfigOverride(Map.class, o -> o.setNullHandling(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY))) // fix null map
                .addModule(new SimpleModule().addDeserializer(String.class, new ValueDeserializer<>() {
                    // fix null values like "key: null"
                    // idk, why withConfigOverride(String.class, ...) doesn't fix it

                    @Override
                    public String deserialize(JsonParser p, DeserializationContext ctxt) {
                        return p.currentToken() == JsonToken.VALUE_NULL ? "" : p.getString();
                    }

                    @Override
                    public String getNullValue(DeserializationContext ctxt) {
                        return "";
                    }

                }))
                .build();
    }

}
