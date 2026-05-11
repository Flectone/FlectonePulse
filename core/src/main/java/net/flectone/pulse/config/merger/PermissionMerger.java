package net.flectone.pulse.config.merger;

import net.flectone.pulse.config.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for merging {@link Permission} configuration objects.
 * <p>
 * This interface defines mapping methods for deep merging permission configurations,
 * handling nested structures through builder patterns.
 * </p>
 *
 * @author TheFaser
 * @since 1.7.1
 */
@Mapper(config = MapstructMergerConfig.class)
public interface PermissionMerger {

    @Mapping(target = "module", expression = "java(mergePermissionEntry(target.build().module().toBuilder(), source.module()))")
    @Mapping(target = "command", expression = "java(mergeCommand(target.build().command().toBuilder(), source.command()))")
    @Mapping(target = "integration", expression = "java(mergeIntegration(target.build().integration().toBuilder(), source.integration()))")
    @Mapping(target = "message", expression = "java(mergeMessage(target.build().message().toBuilder(), source.message()))")
    Permission merge(@MappingTarget Permission.PermissionBuilder target, Permission source);

    Permission.PermissionEntry mergePermissionEntry(@MappingTarget Permission.PermissionEntry.PermissionEntryBuilder target, Permission.PermissionEntry source);

    @Mapping(target = "afk", expression = "java(mergeCommandAfk(target.build().afk().toBuilder(), source.afk()))")
    @Mapping(target = "anon", expression = "java(mergeCommandAnon(target.build().anon().toBuilder(), source.anon()))")
    @Mapping(target = "ball", expression = "java(mergeCommandBall(target.build().ball().toBuilder(), source.ball()))")
    @Mapping(target = "ban", expression = "java(mergeCommandBan(target.build().ban().toBuilder(), source.ban()))")
    @Mapping(target = "banlist", expression = "java(mergeCommandBanlist(target.build().banlist().toBuilder(), source.banlist()))")
    @Mapping(target = "broadcast", expression = "java(mergeCommandBroadcast(target.build().broadcast().toBuilder(), source.broadcast()))")
    @Mapping(target = "chatcolor", expression = "java(mergeCommandChatcolor(target.build().chatcolor().toBuilder(), source.chatcolor()))")
    @Mapping(target = "chatsetting", expression = "java(mergeCommandChatsetting(target.build().chatsetting().toBuilder(), source.chatsetting()))")
    @Mapping(target = "clearchat", expression = "java(mergeCommandClearchat(target.build().clearchat().toBuilder(), source.clearchat()))")
    @Mapping(target = "clearmail", expression = "java(mergeCommandClearmail(target.build().clearmail().toBuilder(), source.clearmail()))")
    @Mapping(target = "coin", expression = "java(mergeCommandCoin(target.build().coin().toBuilder(), source.coin()))")
    @Mapping(target = "deletemessage", expression = "java(mergeCommandDeletemessage(target.build().deletemessage().toBuilder(), source.deletemessage()))")
    @Mapping(target = "dice", expression = "java(mergeCommandDice(target.build().dice().toBuilder(), source.dice()))")
    @Mapping(target = "commandDo", expression = "java(mergeCommandDo(target.build().commandDo().toBuilder(), source.commandDo()))")
    @Mapping(target = "emit", expression = "java(mergeCommandEmit(target.build().emit().toBuilder(), source.emit()))")
    @Mapping(target = "flectonepulse", expression = "java(mergeCommandFlectonepulse(target.build().flectonepulse().toBuilder(), source.flectonepulse()))")
    @Mapping(target = "geolocate", expression = "java(mergeCommandGeolocate(target.build().geolocate().toBuilder(), source.geolocate()))")
    @Mapping(target = "helper", expression = "java(mergeCommandHelper(target.build().helper().toBuilder(), source.helper()))")
    @Mapping(target = "ignore", expression = "java(mergeCommandIgnore(target.build().ignore().toBuilder(), source.ignore()))")
    @Mapping(target = "ignorelist", expression = "java(mergeCommandIgnorelist(target.build().ignorelist().toBuilder(), source.ignorelist()))")
    @Mapping(target = "kick", expression = "java(mergeCommandKick(target.build().kick().toBuilder(), source.kick()))")
    @Mapping(target = "mail", expression = "java(mergeCommandMail(target.build().mail().toBuilder(), source.mail()))")
    @Mapping(target = "maintenance", expression = "java(mergeCommandMaintenance(target.build().maintenance().toBuilder(), source.maintenance()))")
    @Mapping(target = "me", expression = "java(mergeCommandMe(target.build().me().toBuilder(), source.me()))")
    @Mapping(target = "mute", expression = "java(mergeCommandMute(target.build().mute().toBuilder(), source.mute()))")
    @Mapping(target = "mutelist", expression = "java(mergeCommandMutelist(target.build().mutelist().toBuilder(), source.mutelist()))")
    @Mapping(target = "nickname", expression = "java(mergeCommandNickname(target.build().nickname().toBuilder(), source.nickname()))")
    @Mapping(target = "online", expression = "java(mergeCommandOnline(target.build().online().toBuilder(), source.online()))")
    @Mapping(target = "ping", expression = "java(mergeCommandPing(target.build().ping().toBuilder(), source.ping()))")
    @Mapping(target = "poll", expression = "java(mergeCommandPoll(target.build().poll().toBuilder(), source.poll()))")
    @Mapping(target = "reply", expression = "java(mergeCommandReply(target.build().reply().toBuilder(), source.reply()))")
    @Mapping(target = "rockpaperscissors", expression = "java(mergeCommandRockpaperscissors(target.build().rockpaperscissors().toBuilder(), source.rockpaperscissors()))")
    @Mapping(target = "sprite", expression = "java(mergeCommandSprite(target.build().sprite().toBuilder(), source.sprite()))")
    @Mapping(target = "spy", expression = "java(mergeCommandSpy(target.build().spy().toBuilder(), source.spy()))")
    @Mapping(target = "stream", expression = "java(mergeCommandStream(target.build().stream().toBuilder(), source.stream()))")
    @Mapping(target = "symbol", expression = "java(mergeCommandSymbol(target.build().symbol().toBuilder(), source.symbol()))")
    @Mapping(target = "tell", expression = "java(mergeCommandTell(target.build().tell().toBuilder(), source.tell()))")
    @Mapping(target = "tictactoe", expression = "java(mergeCommandTictactoe(target.build().tictactoe().toBuilder(), source.tictactoe()))")
    @Mapping(target = "toponline", expression = "java(mergeCommandToponline(target.build().toponline().toBuilder(), source.toponline()))")
    @Mapping(target = "translateto", expression = "java(mergeCommandTranslateto(target.build().translateto().toBuilder(), source.translateto()))")
    @Mapping(target = "commandTry", expression = "java(mergeCommandTry(target.build().commandTry().toBuilder(), source.commandTry()))")
    @Mapping(target = "unban", expression = "java(mergeCommandUnban(target.build().unban().toBuilder(), source.unban()))")
    @Mapping(target = "unmute", expression = "java(mergeCommandUnmute(target.build().unmute().toBuilder(), source.unmute()))")
    @Mapping(target = "unwarn", expression = "java(mergeCommandUnwarn(target.build().unwarn().toBuilder(), source.unwarn()))")
    @Mapping(target = "warn", expression = "java(mergeCommandWarn(target.build().warn().toBuilder(), source.warn()))")
    @Mapping(target = "warnlist", expression = "java(mergeCommandWarnlist(target.build().warnlist().toBuilder(), source.warnlist()))")
    @Mapping(target = "whitelist", expression = "java(mergeCommandWhitelist(target.build().whitelist().toBuilder(), source.whitelist()))")
    Permission.Command mergeCommand(@MappingTarget Permission.Command.CommandBuilder target, Permission.Command source);

    Permission.Command.Afk mergeCommandAfk(@MappingTarget Permission.Command.Afk.AfkBuilder target, Permission.Command.Afk source);

    Permission.Command.Anon mergeCommandAnon(@MappingTarget Permission.Command.Anon.AnonBuilder target, Permission.Command.Anon source);

    Permission.Command.Ball mergeCommandBall(@MappingTarget Permission.Command.Ball.BallBuilder target, Permission.Command.Ball source);

    Permission.Command.Ban mergeCommandBan(@MappingTarget Permission.Command.Ban.BanBuilder target, Permission.Command.Ban source);

    Permission.Command.Banlist mergeCommandBanlist(@MappingTarget Permission.Command.Banlist.BanlistBuilder target, Permission.Command.Banlist source);

    Permission.Command.Broadcast mergeCommandBroadcast(@MappingTarget Permission.Command.Broadcast.BroadcastBuilder target, Permission.Command.Broadcast source);

    Permission.Command.Chatcolor mergeCommandChatcolor(@MappingTarget Permission.Command.Chatcolor.ChatcolorBuilder target, Permission.Command.Chatcolor source);

    Permission.Command.Chatsetting mergeCommandChatsetting(@MappingTarget Permission.Command.Chatsetting.ChatsettingBuilder target, Permission.Command.Chatsetting source);

    Permission.Command.Clearchat mergeCommandClearchat(@MappingTarget Permission.Command.Clearchat.ClearchatBuilder target, Permission.Command.Clearchat source);

    Permission.Command.Clearmail mergeCommandClearmail(@MappingTarget Permission.Command.Clearmail.ClearmailBuilder target, Permission.Command.Clearmail source);

    Permission.Command.Coin mergeCommandCoin(@MappingTarget Permission.Command.Coin.CoinBuilder target, Permission.Command.Coin source);

    Permission.Command.Deletemessage mergeCommandDeletemessage(@MappingTarget Permission.Command.Deletemessage.DeletemessageBuilder target, Permission.Command.Deletemessage source);

    Permission.Command.Dice mergeCommandDice(@MappingTarget Permission.Command.Dice.DiceBuilder target, Permission.Command.Dice source);

    Permission.Command.CommandDo mergeCommandDo(@MappingTarget Permission.Command.CommandDo.CommandDoBuilder target, Permission.Command.CommandDo source);

    Permission.Command.Emit mergeCommandEmit(@MappingTarget Permission.Command.Emit.EmitBuilder target, Permission.Command.Emit source);

    Permission.Command.Flectonepulse mergeCommandFlectonepulse(@MappingTarget Permission.Command.Flectonepulse.FlectonepulseBuilder target, Permission.Command.Flectonepulse source);

    Permission.Command.Geolocate mergeCommandGeolocate(@MappingTarget Permission.Command.Geolocate.GeolocateBuilder target, Permission.Command.Geolocate source);

    Permission.Command.Helper mergeCommandHelper(@MappingTarget Permission.Command.Helper.HelperBuilder target, Permission.Command.Helper source);

    Permission.Command.Ignore mergeCommandIgnore(@MappingTarget Permission.Command.Ignore.IgnoreBuilder target, Permission.Command.Ignore source);

    Permission.Command.Ignorelist mergeCommandIgnorelist(@MappingTarget Permission.Command.Ignorelist.IgnorelistBuilder target, Permission.Command.Ignorelist source);

    Permission.Command.Kick mergeCommandKick(@MappingTarget Permission.Command.Kick.KickBuilder target, Permission.Command.Kick source);

    Permission.Command.Mail mergeCommandMail(@MappingTarget Permission.Command.Mail.MailBuilder target, Permission.Command.Mail source);

    Permission.Command.Maintenance mergeCommandMaintenance(@MappingTarget Permission.Command.Maintenance.MaintenanceBuilder target, Permission.Command.Maintenance source);

    Permission.Command.Me mergeCommandMe(@MappingTarget Permission.Command.Me.MeBuilder target, Permission.Command.Me source);

    Permission.Command.Mute mergeCommandMute(@MappingTarget Permission.Command.Mute.MuteBuilder target, Permission.Command.Mute source);

    Permission.Command.Mutelist mergeCommandMutelist(@MappingTarget Permission.Command.Mutelist.MutelistBuilder target, Permission.Command.Mutelist source);

    Permission.Command.Nickname mergeCommandNickname(@MappingTarget Permission.Command.Nickname.NicknameBuilder target, Permission.Command.Nickname source);

    Permission.Command.Online mergeCommandOnline(@MappingTarget Permission.Command.Online.OnlineBuilder target, Permission.Command.Online source);

    Permission.Command.Ping mergeCommandPing(@MappingTarget Permission.Command.Ping.PingBuilder target, Permission.Command.Ping source);

    Permission.Command.Poll mergeCommandPoll(@MappingTarget Permission.Command.Poll.PollBuilder target, Permission.Command.Poll source);

    Permission.Command.Reply mergeCommandReply(@MappingTarget Permission.Command.Reply.ReplyBuilder target, Permission.Command.Reply source);

    Permission.Command.Rockpaperscissors mergeCommandRockpaperscissors(@MappingTarget Permission.Command.Rockpaperscissors.RockpaperscissorsBuilder target, Permission.Command.Rockpaperscissors source);

    Permission.Command.Sprite mergeCommandSprite(@MappingTarget Permission.Command.Sprite.SpriteBuilder target, Permission.Command.Sprite source);

    Permission.Command.Spy mergeCommandSpy(@MappingTarget Permission.Command.Spy.SpyBuilder target, Permission.Command.Spy source);

    Permission.Command.Stream mergeCommandStream(@MappingTarget Permission.Command.Stream.StreamBuilder target, Permission.Command.Stream source);

    Permission.Command.Symbol mergeCommandSymbol(@MappingTarget Permission.Command.Symbol.SymbolBuilder target, Permission.Command.Symbol source);

    Permission.Command.Tell mergeCommandTell(@MappingTarget Permission.Command.Tell.TellBuilder target, Permission.Command.Tell source);

    Permission.Command.Tictactoe mergeCommandTictactoe(@MappingTarget Permission.Command.Tictactoe.TictactoeBuilder target, Permission.Command.Tictactoe source);

    Permission.Command.Toponline mergeCommandToponline(@MappingTarget Permission.Command.Toponline.ToponlineBuilder target, Permission.Command.Toponline source);

    Permission.Command.Translateto mergeCommandTranslateto(@MappingTarget Permission.Command.Translateto.TranslatetoBuilder target, Permission.Command.Translateto source);

    Permission.Command.CommandTry mergeCommandTry(@MappingTarget Permission.Command.CommandTry.CommandTryBuilder target, Permission.Command.CommandTry source);

    Permission.Command.Unban mergeCommandUnban(@MappingTarget Permission.Command.Unban.UnbanBuilder target, Permission.Command.Unban source);

    Permission.Command.Unmute mergeCommandUnmute(@MappingTarget Permission.Command.Unmute.UnmuteBuilder target, Permission.Command.Unmute source);

    Permission.Command.Unwarn mergeCommandUnwarn(@MappingTarget Permission.Command.Unwarn.UnwarnBuilder target, Permission.Command.Unwarn source);

    Permission.Command.Warn mergeCommandWarn(@MappingTarget Permission.Command.Warn.WarnBuilder target, Permission.Command.Warn source);

    Permission.Command.Warnlist mergeCommandWarnlist(@MappingTarget Permission.Command.Warnlist.WarnlistBuilder target, Permission.Command.Warnlist source);

    Permission.Command.Whitelist mergeCommandWhitelist(@MappingTarget Permission.Command.Whitelist.WhitelistBuilder target, Permission.Command.Whitelist source);

    @Mapping(target = "advancedban", expression = "java(mergeIntegrationAdvancedban(target.build().advancedban().toBuilder(), source.advancedban()))")
    @Mapping(target = "blazeandcave", expression = "java(mergeIntegrationBlazeandcave(target.build().blazeandcave().toBuilder(), source.blazeandcave()))")
    @Mapping(target = "cmi", expression = "java(mergeIntegrationCmi(target.build().cmi().toBuilder(), source.cmi()))")
    @Mapping(target = "libertybans", expression = "java(mergeIntegrationLibertybans(target.build().libertybans().toBuilder(), source.libertybans()))")
    @Mapping(target = "deepl", expression = "java(mergeIntegrationDeepl(target.build().deepl().toBuilder(), source.deepl()))")
    @Mapping(target = "discord", expression = "java(mergeIntegrationDiscord(target.build().discord().toBuilder(), source.discord()))")
    @Mapping(target = "floodgate", expression = "java(mergeIntegrationFloodgate(target.build().floodgate().toBuilder(), source.floodgate()))")
    @Mapping(target = "geyser", expression = "java(mergeIntegrationGeyser(target.build().geyser().toBuilder(), source.geyser()))")
    @Mapping(target = "icu", expression = "java(mergeIntegrationIcu(target.build().icu().toBuilder(), source.icu()))")
    @Mapping(target = "interactivechat", expression = "java(mergeIntegrationInteractivechat(target.build().interactivechat().toBuilder(), source.interactivechat()))")
    @Mapping(target = "itemsadder", expression = "java(mergeIntegrationItemsadder(target.build().itemsadder().toBuilder(), source.itemsadder()))")
    @Mapping(target = "litebans", expression = "java(mergeIntegrationLitebans(target.build().litebans().toBuilder(), source.litebans()))")
    @Mapping(target = "luckperms", expression = "java(mergeIntegrationLuckperms(target.build().luckperms().toBuilder(), source.luckperms()))")
    @Mapping(target = "maintenance", expression = "java(mergeIntegrationMaintenance(target.build().maintenance().toBuilder(), source.maintenance()))")
    @Mapping(target = "minimotd", expression = "java(mergeIntegrationMiniMOTD(target.build().minimotd().toBuilder(), source.minimotd()))")
    @Mapping(target = "miniplaceholders", expression = "java(mergeIntegrationMiniPlaceholders(target.build().miniplaceholders().toBuilder(), source.miniplaceholders()))")
    @Mapping(target = "motd", expression = "java(mergeIntegrationMOTD(target.build().motd().toBuilder(), source.motd()))")
    @Mapping(target = "placeholderapi", expression = "java(mergeIntegrationPlaceholderapi(target.build().placeholderapi().toBuilder(), source.placeholderapi()))")
    @Mapping(target = "plasmovoice", expression = "java(mergeIntegrationPlasmovoice(target.build().plasmovoice().toBuilder(), source.plasmovoice()))")
    @Mapping(target = "simplevoice", expression = "java(mergeIntegrationSimplevoice(target.build().simplevoice().toBuilder(), source.simplevoice()))")
    @Mapping(target = "skinsrestorer", expression = "java(mergeIntegrationSkinsrestorer(target.build().skinsrestorer().toBuilder(), source.skinsrestorer()))")
    @Mapping(target = "supervanish", expression = "java(mergeIntegrationSupervanish(target.build().supervanish().toBuilder(), source.supervanish()))")
    @Mapping(target = "tab", expression = "java(mergeIntegrationTab(target.build().tab().toBuilder(), source.tab()))")
    @Mapping(target = "telegram", expression = "java(mergeIntegrationTelegram(target.build().telegram().toBuilder(), source.telegram()))")
    @Mapping(target = "triton", expression = "java(mergeIntegrationTriton(target.build().triton().toBuilder(), source.triton()))")
    @Mapping(target = "twitch", expression = "java(mergeIntegrationTwitch(target.build().twitch().toBuilder(), source.twitch()))")
    @Mapping(target = "vault", expression = "java(mergeIntegrationVault(target.build().vault().toBuilder(), source.vault()))")
    @Mapping(target = "yandex", expression = "java(mergeIntegrationYandex(target.build().yandex().toBuilder(), source.yandex()))")
    Permission.Integration mergeIntegration(@MappingTarget Permission.Integration.IntegrationBuilder target, Permission.Integration source);

    Permission.Integration.Advancedban mergeIntegrationAdvancedban(@MappingTarget Permission.Integration.Advancedban.AdvancedbanBuilder target, Permission.Integration.Advancedban source);

    Permission.Integration.Blazeandcave mergeIntegrationBlazeandcave(@MappingTarget Permission.Integration.Blazeandcave.BlazeandcaveBuilder target, Permission.Integration.Blazeandcave source);

    Permission.Integration.CMI mergeIntegrationCmi(@MappingTarget Permission.Integration.CMI.CMIBuilder target, Permission.Integration.CMI source);

    Permission.Integration.Libertybans mergeIntegrationLibertybans(@MappingTarget Permission.Integration.Libertybans.LibertybansBuilder target, Permission.Integration.Libertybans source);

    Permission.Integration.Deepl mergeIntegrationDeepl(@MappingTarget Permission.Integration.Deepl.DeeplBuilder target, Permission.Integration.Deepl source);

    Permission.Integration.Discord mergeIntegrationDiscord(@MappingTarget Permission.Integration.Discord.DiscordBuilder target, Permission.Integration.Discord source);

    Permission.Integration.Floodgate mergeIntegrationFloodgate(@MappingTarget Permission.Integration.Floodgate.FloodgateBuilder target, Permission.Integration.Floodgate source);

    Permission.Integration.Geyser mergeIntegrationGeyser(@MappingTarget Permission.Integration.Geyser.GeyserBuilder target, Permission.Integration.Geyser source);

    Permission.Integration.Icu mergeIntegrationIcu(@MappingTarget Permission.Integration.Icu.IcuBuilder target, Permission.Integration.Icu source);

    Permission.Integration.Interactivechat mergeIntegrationInteractivechat(@MappingTarget Permission.Integration.Interactivechat.InteractivechatBuilder target, Permission.Integration.Interactivechat source);

    Permission.Integration.Itemsadder mergeIntegrationItemsadder(@MappingTarget Permission.Integration.Itemsadder.ItemsadderBuilder target, Permission.Integration.Itemsadder source);

    Permission.Integration.Litebans mergeIntegrationLitebans(@MappingTarget Permission.Integration.Litebans.LitebansBuilder target, Permission.Integration.Litebans source);

    Permission.Integration.Luckperms mergeIntegrationLuckperms(@MappingTarget Permission.Integration.Luckperms.LuckpermsBuilder target, Permission.Integration.Luckperms source);

    Permission.Integration.Maintenance mergeIntegrationMaintenance(@MappingTarget Permission.Integration.Maintenance.MaintenanceBuilder target, Permission.Integration.Maintenance source);

    Permission.Integration.MiniMOTD mergeIntegrationMiniMOTD(@MappingTarget Permission.Integration.MiniMOTD.MiniMOTDBuilder target, Permission.Integration.MiniMOTD source);

    Permission.Integration.MiniPlaceholders mergeIntegrationMiniPlaceholders(@MappingTarget Permission.Integration.MiniPlaceholders.MiniPlaceholdersBuilder target, Permission.Integration.MiniPlaceholders source);

    Permission.Integration.MOTD mergeIntegrationMOTD(@MappingTarget Permission.Integration.MOTD.MOTDBuilder target, Permission.Integration.MOTD source);

    Permission.Integration.Placeholderapi mergeIntegrationPlaceholderapi(@MappingTarget Permission.Integration.Placeholderapi.PlaceholderapiBuilder target, Permission.Integration.Placeholderapi source);

    Permission.Integration.Plasmovoice mergeIntegrationPlasmovoice(@MappingTarget Permission.Integration.Plasmovoice.PlasmovoiceBuilder target, Permission.Integration.Plasmovoice source);

    Permission.Integration.Simplevoice mergeIntegrationSimplevoice(@MappingTarget Permission.Integration.Simplevoice.SimplevoiceBuilder target, Permission.Integration.Simplevoice source);

    Permission.Integration.Skinsrestorer mergeIntegrationSkinsrestorer(@MappingTarget Permission.Integration.Skinsrestorer.SkinsrestorerBuilder target, Permission.Integration.Skinsrestorer source);

    Permission.Integration.Supervanish mergeIntegrationSupervanish(@MappingTarget Permission.Integration.Supervanish.SupervanishBuilder target, Permission.Integration.Supervanish source);

    Permission.Integration.Tab mergeIntegrationTab(@MappingTarget Permission.Integration.Tab.TabBuilder target, Permission.Integration.Tab source);

    Permission.Integration.Telegram mergeIntegrationTelegram(@MappingTarget Permission.Integration.Telegram.TelegramBuilder target, Permission.Integration.Telegram source);

    Permission.Integration.Triton mergeIntegrationTriton(@MappingTarget Permission.Integration.Triton.TritonBuilder target, Permission.Integration.Triton source);

    Permission.Integration.Twitch mergeIntegrationTwitch(@MappingTarget Permission.Integration.Twitch.TwitchBuilder target, Permission.Integration.Twitch source);

    Permission.Integration.Vault mergeIntegrationVault(@MappingTarget Permission.Integration.Vault.VaultBuilder target, Permission.Integration.Vault source);

    Permission.Integration.Yandex mergeIntegrationYandex(@MappingTarget Permission.Integration.Yandex.YandexBuilder target, Permission.Integration.Yandex source);

    @Mapping(target = "afk", expression = "java(mergeMessageAfk(target.build().afk().toBuilder(), source.afk()))")
    @Mapping(target = "anvil", expression = "java(mergeMessageAnvil(target.build().anvil().toBuilder(), source.anvil()))")
    @Mapping(target = "auto", expression = "java(mergeMessageAuto(target.build().auto().toBuilder(), source.auto()))")
    @Mapping(target = "book", expression = "java(mergeMessageBook(target.build().book().toBuilder(), source.book()))")
    @Mapping(target = "bossbar", expression = "java(mergeMessageBossbar(target.build().bossbar().toBuilder(), source.bossbar()))")
    @Mapping(target = "brand", expression = "java(mergeMessageBrand(target.build().brand().toBuilder(), source.brand()))")
    @Mapping(target = "bubble", expression = "java(mergeMessageBubble(target.build().bubble().toBuilder(), source.bubble()))")
    @Mapping(target = "chat", expression = "java(mergeMessageChat(target.build().chat().toBuilder(), source.chat()))")
    @Mapping(target = "format", expression = "java(mergeMessageFormat(target.build().format().toBuilder(), source.format()))")
    @Mapping(target = "greeting", expression = "java(mergeMessageGreeting(target.build().greeting().toBuilder(), source.greeting()))")
    @Mapping(target = "join", expression = "java(mergeMessageJoin(target.build().join().toBuilder(), source.join()))")
    @Mapping(target = "quit", expression = "java(mergeMessageQuit(target.build().quit().toBuilder(), source.quit()))")
    @Mapping(target = "rightclick", expression = "java(mergeMessageRightclick(target.build().rightclick().toBuilder(), source.rightclick()))")
    @Mapping(target = "scoreboard", expression = "java(mergeMessageScoreboard(target.build().scoreboard().toBuilder(), source.scoreboard()))")
    @Mapping(target = "serverlink", expression = "java(mergeMessageServerlink(target.build().serverlink().toBuilder(), source.serverlink()))")
    @Mapping(target = "sidebar", expression = "java(mergeMessageSidebar(target.build().sidebar().toBuilder(), source.sidebar()))")
    @Mapping(target = "sign", expression = "java(mergeMessageSign(target.build().sign().toBuilder(), source.sign()))")
    @Mapping(target = "status", expression = "java(mergeMessageStatus(target.build().status().toBuilder(), source.status()))")
    @Mapping(target = "tab", expression = "java(mergeMessageTab(target.build().tab().toBuilder(), source.tab()))")
    @Mapping(target = "update", expression = "java(mergeMessageUpdate(target.build().update().toBuilder(), source.update()))")
    @Mapping(target = "vanilla", expression = "java(mergeMessageVanilla(target.build().vanilla().toBuilder(), source.vanilla()))")
    Permission.Message mergeMessage(@MappingTarget Permission.Message.MessageBuilder target, Permission.Message source);

    Permission.Message.Afk mergeMessageAfk(@MappingTarget Permission.Message.Afk.AfkBuilder target, Permission.Message.Afk source);

    Permission.Message.Anvil mergeMessageAnvil(@MappingTarget Permission.Message.Anvil.AnvilBuilder target, Permission.Message.Anvil source);

    Permission.Message.Auto mergeMessageAuto(@MappingTarget Permission.Message.Auto.AutoBuilder target, Permission.Message.Auto source);

    Permission.Message.Book mergeMessageBook(@MappingTarget Permission.Message.Book.BookBuilder target, Permission.Message.Book source);

    Permission.Message.Bossbar mergeMessageBossbar(@MappingTarget Permission.Message.Bossbar.BossbarBuilder target, Permission.Message.Bossbar source);

    Permission.Message.Brand mergeMessageBrand(@MappingTarget Permission.Message.Brand.BrandBuilder target, Permission.Message.Brand source);

    Permission.Message.Bubble mergeMessageBubble(@MappingTarget Permission.Message.Bubble.BubbleBuilder target, Permission.Message.Bubble source);

    Permission.Message.Chat mergeMessageChat(@MappingTarget Permission.Message.Chat.ChatBuilder target, Permission.Message.Chat source);

    Permission.Message.Greeting mergeMessageGreeting(@MappingTarget Permission.Message.Greeting.GreetingBuilder target, Permission.Message.Greeting source);

    Permission.Message.Join mergeMessageJoin(@MappingTarget Permission.Message.Join.JoinBuilder target, Permission.Message.Join source);

    Permission.Message.Quit mergeMessageQuit(@MappingTarget Permission.Message.Quit.QuitBuilder target, Permission.Message.Quit source);

    Permission.Message.Rightclick mergeMessageRightclick(@MappingTarget Permission.Message.Rightclick.RightclickBuilder target, Permission.Message.Rightclick source);

    @Mapping(target = "objective", expression = "java(mergeMessageScoreboardObjective(target.build().objective().toBuilder(), source.objective()))")
    Permission.Message.Scoreboard mergeMessageScoreboard(@MappingTarget Permission.Message.Scoreboard.ScoreboardBuilder target, Permission.Message.Scoreboard source);

    @Mapping(target = "belowname", expression = "java(mergeMessageScoreboardObjectiveBelowname(target.build().belowname().toBuilder(), source.belowname()))")
    @Mapping(target = "tabname", expression = "java(mergeMessageScoreboardObjectiveTabname(target.build().tabname().toBuilder(), source.tabname()))")
    Permission.Message.Scoreboard.Objective mergeMessageScoreboardObjective(@MappingTarget Permission.Message.Scoreboard.Objective.ObjectiveBuilder target, Permission.Message.Scoreboard.Objective source);

    Permission.Message.Scoreboard.Objective.Belowname mergeMessageScoreboardObjectiveBelowname(@MappingTarget Permission.Message.Scoreboard.Objective.Belowname.BelownameBuilder target, Permission.Message.Scoreboard.Objective.Belowname source);

    Permission.Message.Scoreboard.Objective.Tabname mergeMessageScoreboardObjectiveTabname(@MappingTarget Permission.Message.Scoreboard.Objective.Tabname.TabnameBuilder target, Permission.Message.Scoreboard.Objective.Tabname source);

    Permission.Message.Serverlink mergeMessageServerlink(@MappingTarget Permission.Message.Serverlink.ServerlinkBuilder target, Permission.Message.Serverlink source);

    Permission.Message.Sidebar mergeMessageSidebar(@MappingTarget Permission.Message.Sidebar.SidebarBuilder target, Permission.Message.Sidebar source);

    Permission.Message.Sign mergeMessageSign(@MappingTarget Permission.Message.Sign.SignBuilder target, Permission.Message.Sign source);

    Permission.Message.Update mergeMessageUpdate(@MappingTarget Permission.Message.Update.UpdateBuilder target, Permission.Message.Update source);

    Permission.Message.Vanilla mergeMessageVanilla(@MappingTarget Permission.Message.Vanilla.VanillaBuilder target, Permission.Message.Vanilla source);

    @Mapping(target = "animation", expression = "java(mergeMessageFormatAnimation(target.build().animation().toBuilder(), source.animation()))")
    @Mapping(target = "condition", expression = "java(mergeMessageFormatCondition(target.build().condition().toBuilder(), source.condition()))")
    @Mapping(target = "fcolor", expression = "java(mergeMessageFormatFColor(target.build().fcolor().toBuilder(), source.fcolor()))")
    @Mapping(target = "fixation", expression = "java(mergeMessageFormatFixation(target.build().fixation().toBuilder(), source.fixation()))")
    @Mapping(target = "mention", expression = "java(mergeMessageFormatMention(target.build().mention().toBuilder(), source.mention()))")
    @Mapping(target = "moderation", expression = "java(mergeMessageFormatModeration(target.build().moderation().toBuilder(), source.moderation()))")
    @Mapping(target = "names", expression = "java(mergeMessageFormatNames(target.build().names().toBuilder(), source.names()))")
    @Mapping(target = "object", expression = "java(mergeMessageFormatObject(target.build().object().toBuilder(), source.object()))")
    @Mapping(target = "questionAnswer", expression = "java(mergeMessageFormatQuestionAnswer(target.build().questionAnswer().toBuilder(), source.questionAnswer()))")
    @Mapping(target = "replacement", expression = "java(mergeMessageFormatReplacement(target.build().replacement().toBuilder(), source.replacement()))")
    @Mapping(target = "translate", expression = "java(mergeMessageFormatTranslate(target.build().translate().toBuilder(), source.translate()))")
    @Mapping(target = "world", expression = "java(mergeMessageFormatWorld(target.build().world().toBuilder(), source.world()))")
    Permission.Message.Format mergeMessageFormat(@MappingTarget Permission.Message.Format.FormatBuilder target, Permission.Message.Format source);

    Permission.Message.Format.Animation mergeMessageFormatAnimation(@MappingTarget Permission.Message.Format.Animation.AnimationBuilder target, Permission.Message.Format.Animation source);

    Permission.Message.Format.Condition mergeMessageFormatCondition(@MappingTarget Permission.Message.Format.Condition.ConditionBuilder target, Permission.Message.Format.Condition source);

    Permission.Message.Format.FColor mergeMessageFormatFColor(@MappingTarget Permission.Message.Format.FColor.FColorBuilder target, Permission.Message.Format.FColor source);

    Permission.Message.Format.Fixation mergeMessageFormatFixation(@MappingTarget Permission.Message.Format.Fixation.FixationBuilder target, Permission.Message.Format.Fixation source);

    Permission.Message.Format.Mention mergeMessageFormatMention(@MappingTarget Permission.Message.Format.Mention.MentionBuilder target, Permission.Message.Format.Mention source);

    @Mapping(target = "caps", expression = "java(mergeMessageFormatModerationCaps(target.build().caps().toBuilder(), source.caps()))")
    @Mapping(target = "delete", expression = "java(mergeMessageFormatModerationDelete(target.build().delete().toBuilder(), source.delete()))")
    @Mapping(target = "newbie", expression = "java(mergeMessageFormatModerationNewbie(target.build().newbie().toBuilder(), source.newbie()))")
    @Mapping(target = "flood", expression = "java(mergeMessageFormatModerationFlood(target.build().flood().toBuilder(), source.flood()))")
    @Mapping(target = "swear", expression = "java(mergeMessageFormatModerationSwear(target.build().swear().toBuilder(), source.swear()))")
    Permission.Message.Format.Moderation mergeMessageFormatModeration(@MappingTarget Permission.Message.Format.Moderation.ModerationBuilder target, Permission.Message.Format.Moderation source);

    Permission.Message.Format.Moderation.Caps mergeMessageFormatModerationCaps(@MappingTarget Permission.Message.Format.Moderation.Caps.CapsBuilder target, Permission.Message.Format.Moderation.Caps source);

    Permission.Message.Format.Moderation.Delete mergeMessageFormatModerationDelete(@MappingTarget Permission.Message.Format.Moderation.Delete.DeleteBuilder target, Permission.Message.Format.Moderation.Delete source);

    Permission.Message.Format.Moderation.Newbie mergeMessageFormatModerationNewbie(@MappingTarget Permission.Message.Format.Moderation.Newbie.NewbieBuilder target, Permission.Message.Format.Moderation.Newbie source);

    Permission.Message.Format.Moderation.Flood mergeMessageFormatModerationFlood(@MappingTarget Permission.Message.Format.Moderation.Flood.FloodBuilder target, Permission.Message.Format.Moderation.Flood source);

    Permission.Message.Format.Moderation.Swear mergeMessageFormatModerationSwear(@MappingTarget Permission.Message.Format.Moderation.Swear.SwearBuilder target, Permission.Message.Format.Moderation.Swear source);

    Permission.Message.Format.Names mergeMessageFormatNames(@MappingTarget Permission.Message.Format.Names.NamesBuilder target, Permission.Message.Format.Names source);

    Permission.Message.Format.Object mergeMessageFormatObject(@MappingTarget Permission.Message.Format.Object.ObjectBuilder target, Permission.Message.Format.Object source);

    Permission.Message.Format.QuestionAnswer mergeMessageFormatQuestionAnswer(@MappingTarget Permission.Message.Format.QuestionAnswer.QuestionAnswerBuilder target, Permission.Message.Format.QuestionAnswer source);

    Permission.Message.Format.Replacement mergeMessageFormatReplacement(@MappingTarget Permission.Message.Format.Replacement.ReplacementBuilder target, Permission.Message.Format.Replacement source);

    Permission.Message.Format.Translate mergeMessageFormatTranslate(@MappingTarget Permission.Message.Format.Translate.TranslateBuilder target, Permission.Message.Format.Translate source);

    Permission.Message.Format.World mergeMessageFormatWorld(@MappingTarget Permission.Message.Format.World.WorldBuilder target, Permission.Message.Format.World source);

    @Mapping(target = "icon", expression = "java(mergeMessageStatusIcon(target.build().icon().toBuilder(), source.icon()))")
    @Mapping(target = "motd", expression = "java(mergeMessageStatusMOTD(target.build().motd().toBuilder(), source.motd()))")
    @Mapping(target = "players", expression = "java(mergeMessageStatusPlayers(target.build().players().toBuilder(), source.players()))")
    @Mapping(target = "version", expression = "java(mergeMessageStatusVersion(target.build().version().toBuilder(), source.version()))")
    Permission.Message.Status mergeMessageStatus(@MappingTarget Permission.Message.Status.StatusBuilder target, Permission.Message.Status source);

    Permission.Message.Status.MOTD mergeMessageStatusMOTD(@MappingTarget Permission.Message.Status.MOTD.MOTDBuilder target, Permission.Message.Status.MOTD source);

    Permission.Message.Status.Icon mergeMessageStatusIcon(@MappingTarget Permission.Message.Status.Icon.IconBuilder target, Permission.Message.Status.Icon source);

    Permission.Message.Status.Players mergeMessageStatusPlayers(@MappingTarget Permission.Message.Status.Players.PlayersBuilder target, Permission.Message.Status.Players source);

    Permission.Message.Status.Version mergeMessageStatusVersion(@MappingTarget Permission.Message.Status.Version.VersionBuilder target, Permission.Message.Status.Version source);

    @Mapping(target = "footer", expression = "java(mergeMessageTabFooter(target.build().footer().toBuilder(), source.footer()))")
    @Mapping(target = "header", expression = "java(mergeMessageTabHeader(target.build().header().toBuilder(), source.header()))")
    @Mapping(target = "playerlistname", expression = "java(mergeMessageTabPlayerlistname(target.build().playerlistname().toBuilder(), source.playerlistname()))")
    Permission.Message.Tab mergeMessageTab(@MappingTarget Permission.Message.Tab.TabBuilder target, Permission.Message.Tab source);

    Permission.Message.Tab.Footer mergeMessageTabFooter(@MappingTarget Permission.Message.Tab.Footer.FooterBuilder target, Permission.Message.Tab.Footer source);

    Permission.Message.Tab.Header mergeMessageTabHeader(@MappingTarget Permission.Message.Tab.Header.HeaderBuilder target, Permission.Message.Tab.Header source);

    Permission.Message.Tab.Playerlistname mergeMessageTabPlayerlistname(@MappingTarget Permission.Message.Tab.Playerlistname.PlayerlistnameBuilder target, Permission.Message.Tab.Playerlistname source);

}