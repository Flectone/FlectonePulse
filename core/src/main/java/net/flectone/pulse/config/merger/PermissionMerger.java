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

    Permission.PermissionEntry mergePermissionEntry(@MappingTarget Permission.PermissionEntry.PermissionEntryBuilder target, Permission.PermissionEntry entry);

    @Mapping(target = "afk", expression = "java(mergeAfk(target.build().afk().toBuilder(), source.afk()))")
    @Mapping(target = "anon", expression = "java(mergeAnon(target.build().anon().toBuilder(), source.anon()))")
    @Mapping(target = "ball", expression = "java(mergeBall(target.build().ball().toBuilder(), source.ball()))")
    @Mapping(target = "ban", expression = "java(mergeBan(target.build().ban().toBuilder(), source.ban()))")
    @Mapping(target = "banlist", expression = "java(mergeBanlist(target.build().banlist().toBuilder(), source.banlist()))")
    @Mapping(target = "broadcast", expression = "java(mergeBroadcast(target.build().broadcast().toBuilder(), source.broadcast()))")
    @Mapping(target = "chatcolor", expression = "java(mergeChatcolor(target.build().chatcolor().toBuilder(), source.chatcolor()))")
    @Mapping(target = "chatsetting", expression = "java(mergeChatsetting(target.build().chatsetting().toBuilder(), source.chatsetting()))")
    @Mapping(target = "clearchat", expression = "java(mergeClearchat(target.build().clearchat().toBuilder(), source.clearchat()))")
    @Mapping(target = "clearmail", expression = "java(mergeClearmail(target.build().clearmail().toBuilder(), source.clearmail()))")
    @Mapping(target = "coin", expression = "java(mergeCoin(target.build().coin().toBuilder(), source.coin()))")
    @Mapping(target = "deletemessage", expression = "java(mergeDeletemessage(target.build().deletemessage().toBuilder(), source.deletemessage()))")
    @Mapping(target = "dice", expression = "java(mergeDice(target.build().dice().toBuilder(), source.dice()))")
    @Mapping(target = "commandDo", expression = "java(mergeCommandDo(target.build().commandDo().toBuilder(), source.commandDo()))")
    @Mapping(target = "emit", expression = "java(mergeEmit(target.build().emit().toBuilder(), source.emit()))")
    @Mapping(target = "flectonepulse", expression = "java(mergeFlectonepulse(target.build().flectonepulse().toBuilder(), source.flectonepulse()))")
    @Mapping(target = "geolocate", expression = "java(mergeGeolocate(target.build().geolocate().toBuilder(), source.geolocate()))")
    @Mapping(target = "helper", expression = "java(mergeHelper(target.build().helper().toBuilder(), source.helper()))")
    @Mapping(target = "ignore", expression = "java(mergeIgnore(target.build().ignore().toBuilder(), source.ignore()))")
    @Mapping(target = "ignorelist", expression = "java(mergeIgnorelist(target.build().ignorelist().toBuilder(), source.ignorelist()))")
    @Mapping(target = "kick", expression = "java(mergeKick(target.build().kick().toBuilder(), source.kick()))")
    @Mapping(target = "mail", expression = "java(mergeMail(target.build().mail().toBuilder(), source.mail()))")
    @Mapping(target = "maintenance", expression = "java(mergeMaintenance(target.build().maintenance().toBuilder(), source.maintenance()))")
    @Mapping(target = "me", expression = "java(mergeMe(target.build().me().toBuilder(), source.me()))")
    @Mapping(target = "mute", expression = "java(mergeMute(target.build().mute().toBuilder(), source.mute()))")
    @Mapping(target = "mutelist", expression = "java(mergeMutelist(target.build().mutelist().toBuilder(), source.mutelist()))")
    @Mapping(target = "online", expression = "java(mergeOnline(target.build().online().toBuilder(), source.online()))")
    @Mapping(target = "ping", expression = "java(mergePing(target.build().ping().toBuilder(), source.ping()))")
    @Mapping(target = "poll", expression = "java(mergePoll(target.build().poll().toBuilder(), source.poll()))")
    @Mapping(target = "reply", expression = "java(mergeReply(target.build().reply().toBuilder(), source.reply()))")
    @Mapping(target = "rockpaperscissors", expression = "java(mergeRockpaperscissors(target.build().rockpaperscissors().toBuilder(), source.rockpaperscissors()))")
    @Mapping(target = "sprite", expression = "java(mergeSprite(target.build().sprite().toBuilder(), source.sprite()))")
    @Mapping(target = "spy", expression = "java(mergeSpy(target.build().spy().toBuilder(), source.spy()))")
    @Mapping(target = "stream", expression = "java(mergeStream(target.build().stream().toBuilder(), source.stream()))")
    @Mapping(target = "symbol", expression = "java(mergeSymbol(target.build().symbol().toBuilder(), source.symbol()))")
    @Mapping(target = "tell", expression = "java(mergeTell(target.build().tell().toBuilder(), source.tell()))")
    @Mapping(target = "tictactoe", expression = "java(mergeTictactoe(target.build().tictactoe().toBuilder(), source.tictactoe()))")
    @Mapping(target = "toponline", expression = "java(mergeToponline(target.build().toponline().toBuilder(), source.toponline()))")
    @Mapping(target = "translateto", expression = "java(mergeTranslateto(target.build().translateto().toBuilder(), source.translateto()))")
    @Mapping(target = "commandTry", expression = "java(mergeCommandTry(target.build().commandTry().toBuilder(), source.commandTry()))")
    @Mapping(target = "unban", expression = "java(mergeUnban(target.build().unban().toBuilder(), source.unban()))")
    @Mapping(target = "unmute", expression = "java(mergeUnmute(target.build().unmute().toBuilder(), source.unmute()))")
    @Mapping(target = "unwarn", expression = "java(mergeUnwarn(target.build().unwarn().toBuilder(), source.unwarn()))")
    @Mapping(target = "warn", expression = "java(mergeWarn(target.build().warn().toBuilder(), source.warn()))")
    @Mapping(target = "warnlist", expression = "java(mergeWarnlist(target.build().warnlist().toBuilder(), source.warnlist()))")
    Permission.Command mergeCommand(@MappingTarget Permission.Command.CommandBuilder target, Permission.Command source);

    Permission.Command.Afk mergeAfk(@MappingTarget Permission.Command.Afk.AfkBuilder target, Permission.Command.Afk afk);

    Permission.Command.Anon mergeAnon(@MappingTarget Permission.Command.Anon.AnonBuilder target, Permission.Command.Anon anon);

    Permission.Command.Ball mergeBall(@MappingTarget Permission.Command.Ball.BallBuilder target, Permission.Command.Ball ball);

    Permission.Command.Ban mergeBan(@MappingTarget Permission.Command.Ban.BanBuilder target, Permission.Command.Ban ban);

    Permission.Command.Banlist mergeBanlist(@MappingTarget Permission.Command.Banlist.BanlistBuilder target, Permission.Command.Banlist banlist);

    Permission.Command.Broadcast mergeBroadcast(@MappingTarget Permission.Command.Broadcast.BroadcastBuilder target, Permission.Command.Broadcast broadcast);

    Permission.Command.Chatcolor mergeChatcolor(@MappingTarget Permission.Command.Chatcolor.ChatcolorBuilder target, Permission.Command.Chatcolor chatcolor);

    Permission.Command.Chatsetting mergeChatsetting(@MappingTarget Permission.Command.Chatsetting.ChatsettingBuilder target, Permission.Command.Chatsetting chatsetting);

    Permission.Command.Clearchat mergeClearchat(@MappingTarget Permission.Command.Clearchat.ClearchatBuilder target, Permission.Command.Clearchat clearchat);

    Permission.Command.Clearmail mergeClearmail(@MappingTarget Permission.Command.Clearmail.ClearmailBuilder target, Permission.Command.Clearmail clearmail);

    Permission.Command.Coin mergeCoin(@MappingTarget Permission.Command.Coin.CoinBuilder target, Permission.Command.Coin coin);

    Permission.Command.Deletemessage mergeDeletemessage(@MappingTarget Permission.Command.Deletemessage.DeletemessageBuilder target, Permission.Command.Deletemessage deletemessage);

    Permission.Command.Dice mergeDice(@MappingTarget Permission.Command.Dice.DiceBuilder target, Permission.Command.Dice dice);

    Permission.Command.CommandDo mergeCommandDo(@MappingTarget Permission.Command.CommandDo.CommandDoBuilder target, Permission.Command.CommandDo commandDo);

    Permission.Command.Emit mergeEmit(@MappingTarget Permission.Command.Emit.EmitBuilder target, Permission.Command.Emit emit);

    Permission.Command.Flectonepulse mergeFlectonepulse(@MappingTarget Permission.Command.Flectonepulse.FlectonepulseBuilder target, Permission.Command.Flectonepulse flectonepulse);

    Permission.Command.Geolocate mergeGeolocate(@MappingTarget Permission.Command.Geolocate.GeolocateBuilder target, Permission.Command.Geolocate geolocate);

    Permission.Command.Helper mergeHelper(@MappingTarget Permission.Command.Helper.HelperBuilder target, Permission.Command.Helper helper);

    Permission.Command.Ignore mergeIgnore(@MappingTarget Permission.Command.Ignore.IgnoreBuilder target, Permission.Command.Ignore ignore);

    Permission.Command.Ignorelist mergeIgnorelist(@MappingTarget Permission.Command.Ignorelist.IgnorelistBuilder target, Permission.Command.Ignorelist ignorelist);

    Permission.Command.Kick mergeKick(@MappingTarget Permission.Command.Kick.KickBuilder target, Permission.Command.Kick kick);

    Permission.Command.Mail mergeMail(@MappingTarget Permission.Command.Mail.MailBuilder target, Permission.Command.Mail mail);

    Permission.Command.Maintenance mergeMaintenance(@MappingTarget Permission.Command.Maintenance.MaintenanceBuilder target, Permission.Command.Maintenance maintenance);

    Permission.Command.Me mergeMe(@MappingTarget Permission.Command.Me.MeBuilder target, Permission.Command.Me me);

    Permission.Command.Mute mergeMute(@MappingTarget Permission.Command.Mute.MuteBuilder target, Permission.Command.Mute mute);

    Permission.Command.Mutelist mergeMutelist(@MappingTarget Permission.Command.Mutelist.MutelistBuilder target, Permission.Command.Mutelist mutelist);

    Permission.Command.Online mergeOnline(@MappingTarget Permission.Command.Online.OnlineBuilder target, Permission.Command.Online online);

    Permission.Command.Ping mergePing(@MappingTarget Permission.Command.Ping.PingBuilder target, Permission.Command.Ping ping);

    Permission.Command.Poll mergePoll(@MappingTarget Permission.Command.Poll.PollBuilder target, Permission.Command.Poll poll);

    Permission.Command.Reply mergeReply(@MappingTarget Permission.Command.Reply.ReplyBuilder target, Permission.Command.Reply reply);

    Permission.Command.Rockpaperscissors mergeRockpaperscissors(@MappingTarget Permission.Command.Rockpaperscissors.RockpaperscissorsBuilder target, Permission.Command.Rockpaperscissors rockpaperscissors);

    Permission.Command.Sprite mergeSprite(@MappingTarget Permission.Command.Sprite.SpriteBuilder target, Permission.Command.Sprite sprite);

    Permission.Command.Spy mergeSpy(@MappingTarget Permission.Command.Spy.SpyBuilder target, Permission.Command.Spy spy);

    Permission.Command.Stream mergeStream(@MappingTarget Permission.Command.Stream.StreamBuilder target, Permission.Command.Stream stream);

    Permission.Command.Symbol mergeSymbol(@MappingTarget Permission.Command.Symbol.SymbolBuilder target, Permission.Command.Symbol symbol);

    Permission.Command.Tell mergeTell(@MappingTarget Permission.Command.Tell.TellBuilder target, Permission.Command.Tell tell);

    Permission.Command.Tictactoe mergeTictactoe(@MappingTarget Permission.Command.Tictactoe.TictactoeBuilder target, Permission.Command.Tictactoe tictactoe);

    Permission.Command.Toponline mergeToponline(@MappingTarget Permission.Command.Toponline.ToponlineBuilder target, Permission.Command.Toponline toponline);

    Permission.Command.Translateto mergeTranslateto(@MappingTarget Permission.Command.Translateto.TranslatetoBuilder target, Permission.Command.Translateto translateto);

    Permission.Command.CommandTry mergeCommandTry(@MappingTarget Permission.Command.CommandTry.CommandTryBuilder target, Permission.Command.CommandTry commandTry);

    Permission.Command.Unban mergeUnban(@MappingTarget Permission.Command.Unban.UnbanBuilder target, Permission.Command.Unban unban);

    Permission.Command.Unmute mergeUnmute(@MappingTarget Permission.Command.Unmute.UnmuteBuilder target, Permission.Command.Unmute unmute);

    Permission.Command.Unwarn mergeUnwarn(@MappingTarget Permission.Command.Unwarn.UnwarnBuilder target, Permission.Command.Unwarn unwarn);

    Permission.Command.Warn mergeWarn(@MappingTarget Permission.Command.Warn.WarnBuilder target, Permission.Command.Warn warn);

    Permission.Command.Warnlist mergeWarnlist(@MappingTarget Permission.Command.Warnlist.WarnlistBuilder target, Permission.Command.Warnlist warnlist);

    @Mapping(target = "advancedban", expression = "java(mergeAdvancedban(target.build().advancedban().toBuilder(), source.advancedban()))")
    @Mapping(target = "libertybans", expression = "java(mergeLibertybans(target.build().libertybans().toBuilder(), source.libertybans()))")
    @Mapping(target = "deepl", expression = "java(mergeDeepl(target.build().deepl().toBuilder(), source.deepl()))")
    @Mapping(target = "discord", expression = "java(mergeDiscord(target.build().discord().toBuilder(), source.discord()))")
    @Mapping(target = "floodgate", expression = "java(mergeFloodgate(target.build().floodgate().toBuilder(), source.floodgate()))")
    @Mapping(target = "geyser", expression = "java(mergeGeyser(target.build().geyser().toBuilder(), source.geyser()))")
    @Mapping(target = "interactivechat", expression = "java(mergeInteractivechat(target.build().interactivechat().toBuilder(), source.interactivechat()))")
    @Mapping(target = "itemsadder", expression = "java(mergeItemsadder(target.build().itemsadder().toBuilder(), source.itemsadder()))")
    @Mapping(target = "litebans", expression = "java(mergeLitebans(target.build().litebans().toBuilder(), source.litebans()))")
    @Mapping(target = "luckperms", expression = "java(mergeLuckperms(target.build().luckperms().toBuilder(), source.luckperms()))")
    @Mapping(target = "maintenance", expression = "java(mergeIntegrationMaintenance(target.build().maintenance().toBuilder(), source.maintenance()))")
    @Mapping(target = "minimotd", expression = "java(mergeMiniMOTD(target.build().minimotd().toBuilder(), source.minimotd()))")
    @Mapping(target = "miniplaceholders", expression = "java(mergeMiniPlaceholders(target.build().miniplaceholders().toBuilder(), source.miniplaceholders()))")
    @Mapping(target = "motd", expression = "java(mergeMOTD(target.build().motd().toBuilder(), source.motd()))")
    @Mapping(target = "placeholderapi", expression = "java(mergePlaceholderapi(target.build().placeholderapi().toBuilder(), source.placeholderapi()))")
    @Mapping(target = "plasmovoice", expression = "java(mergePlasmovoice(target.build().plasmovoice().toBuilder(), source.plasmovoice()))")
    @Mapping(target = "simplevoice", expression = "java(mergeSimplevoice(target.build().simplevoice().toBuilder(), source.simplevoice()))")
    @Mapping(target = "skinsrestorer", expression = "java(mergeSkinsrestorer(target.build().skinsrestorer().toBuilder(), source.skinsrestorer()))")
    @Mapping(target = "supervanish", expression = "java(mergeSupervanish(target.build().supervanish().toBuilder(), source.supervanish()))")
    @Mapping(target = "tab", expression = "java(mergeTab(target.build().tab().toBuilder(), source.tab()))")
    @Mapping(target = "telegram", expression = "java(mergeTelegram(target.build().telegram().toBuilder(), source.telegram()))")
    @Mapping(target = "triton", expression = "java(mergeTriton(target.build().triton().toBuilder(), source.triton()))")
    @Mapping(target = "twitch", expression = "java(mergeTwitch(target.build().twitch().toBuilder(), source.twitch()))")
    @Mapping(target = "vault", expression = "java(mergeVault(target.build().vault().toBuilder(), source.vault()))")
    @Mapping(target = "yandex", expression = "java(mergeYandex(target.build().yandex().toBuilder(), source.yandex()))")
    Permission.Integration mergeIntegration(@MappingTarget Permission.Integration.IntegrationBuilder target, Permission.Integration source);

    Permission.Integration.Advancedban mergeAdvancedban(@MappingTarget Permission.Integration.Advancedban.AdvancedbanBuilder target, Permission.Integration.Advancedban advancedban);

    Permission.Integration.Libertybans mergeLibertybans(@MappingTarget Permission.Integration.Libertybans.LibertybansBuilder target, Permission.Integration.Libertybans libertybans);

    Permission.Integration.Deepl mergeDeepl(@MappingTarget Permission.Integration.Deepl.DeeplBuilder target, Permission.Integration.Deepl deepl);

    Permission.Integration.Discord mergeDiscord(@MappingTarget Permission.Integration.Discord.DiscordBuilder target, Permission.Integration.Discord discord);

    Permission.Integration.Floodgate mergeFloodgate(@MappingTarget Permission.Integration.Floodgate.FloodgateBuilder target, Permission.Integration.Floodgate floodgate);

    Permission.Integration.Geyser mergeGeyser(@MappingTarget Permission.Integration.Geyser.GeyserBuilder target, Permission.Integration.Geyser geyser);

    Permission.Integration.Interactivechat mergeInteractivechat(@MappingTarget Permission.Integration.Interactivechat.InteractivechatBuilder target, Permission.Integration.Interactivechat interactivechat);

    Permission.Integration.Itemsadder mergeItemsadder(@MappingTarget Permission.Integration.Itemsadder.ItemsadderBuilder target, Permission.Integration.Itemsadder itemsadder);

    Permission.Integration.Litebans mergeLitebans(@MappingTarget Permission.Integration.Litebans.LitebansBuilder target, Permission.Integration.Litebans litebans);

    Permission.Integration.Luckperms mergeLuckperms(@MappingTarget Permission.Integration.Luckperms.LuckpermsBuilder target, Permission.Integration.Luckperms luckperms);

    Permission.Integration.Maintenance mergeIntegrationMaintenance(@MappingTarget Permission.Integration.Maintenance.MaintenanceBuilder target, Permission.Integration.Maintenance maintenance);

    Permission.Integration.MiniMOTD mergeMiniMOTD(@MappingTarget Permission.Integration.MiniMOTD.MiniMOTDBuilder target, Permission.Integration.MiniMOTD minimotd);

    Permission.Integration.MiniPlaceholders mergeMiniPlaceholders(@MappingTarget Permission.Integration.MiniPlaceholders.MiniPlaceholdersBuilder target, Permission.Integration.MiniPlaceholders miniplaceholders);

    Permission.Integration.MOTD mergeMOTD(@MappingTarget Permission.Integration.MOTD.MOTDBuilder target, Permission.Integration.MOTD motd);

    Permission.Integration.Placeholderapi mergePlaceholderapi(@MappingTarget Permission.Integration.Placeholderapi.PlaceholderapiBuilder target, Permission.Integration.Placeholderapi placeholderapi);

    Permission.Integration.Plasmovoice mergePlasmovoice(@MappingTarget Permission.Integration.Plasmovoice.PlasmovoiceBuilder target, Permission.Integration.Plasmovoice plasmovoice);

    Permission.Integration.Simplevoice mergeSimplevoice(@MappingTarget Permission.Integration.Simplevoice.SimplevoiceBuilder target, Permission.Integration.Simplevoice simplevoice);

    Permission.Integration.Skinsrestorer mergeSkinsrestorer(@MappingTarget Permission.Integration.Skinsrestorer.SkinsrestorerBuilder target, Permission.Integration.Skinsrestorer skinsrestorer);

    Permission.Integration.Supervanish mergeSupervanish(@MappingTarget Permission.Integration.Supervanish.SupervanishBuilder target, Permission.Integration.Supervanish supervanish);

    Permission.Integration.Tab mergeTab(@MappingTarget Permission.Integration.Tab.TabBuilder target, Permission.Integration.Tab tab);

    Permission.Integration.Telegram mergeTelegram(@MappingTarget Permission.Integration.Telegram.TelegramBuilder target, Permission.Integration.Telegram telegram);

    Permission.Integration.Triton mergeTriton(@MappingTarget Permission.Integration.Triton.TritonBuilder target, Permission.Integration.Triton triton);

    Permission.Integration.Twitch mergeTwitch(@MappingTarget Permission.Integration.Twitch.TwitchBuilder target, Permission.Integration.Twitch twitch);

    Permission.Integration.Vault mergeVault(@MappingTarget Permission.Integration.Vault.VaultBuilder target, Permission.Integration.Vault vault);

    Permission.Integration.Yandex mergeYandex(@MappingTarget Permission.Integration.Yandex.YandexBuilder target, Permission.Integration.Yandex yandex);

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
    @Mapping(target = "objective", expression = "java(mergeMessageObjective(target.build().objective().toBuilder(), source.objective()))")
    @Mapping(target = "quit", expression = "java(mergeMessageQuit(target.build().quit().toBuilder(), source.quit()))")
    @Mapping(target = "rightclick", expression = "java(mergeMessageRightclick(target.build().rightclick().toBuilder(), source.rightclick()))")
    @Mapping(target = "sidebar", expression = "java(mergeMessageSidebar(target.build().sidebar().toBuilder(), source.sidebar()))")
    @Mapping(target = "sign", expression = "java(mergeMessageSign(target.build().sign().toBuilder(), source.sign()))")
    @Mapping(target = "status", expression = "java(mergeMessageStatus(target.build().status().toBuilder(), source.status()))")
    @Mapping(target = "tab", expression = "java(mergeMessageTab(target.build().tab().toBuilder(), source.tab()))")
    @Mapping(target = "update", expression = "java(mergeMessageUpdate(target.build().update().toBuilder(), source.update()))")
    @Mapping(target = "vanilla", expression = "java(mergeMessageVanilla(target.build().vanilla().toBuilder(), source.vanilla()))")
    Permission.Message mergeMessage(@MappingTarget Permission.Message.MessageBuilder target, Permission.Message source);

    Permission.Message.Afk mergeMessageAfk(@MappingTarget Permission.Message.Afk.AfkBuilder target, Permission.Message.Afk afk);

    Permission.Message.Anvil mergeMessageAnvil(@MappingTarget Permission.Message.Anvil.AnvilBuilder target, Permission.Message.Anvil anvil);

    Permission.Message.Auto mergeMessageAuto(@MappingTarget Permission.Message.Auto.AutoBuilder target, Permission.Message.Auto auto);

    Permission.Message.Book mergeMessageBook(@MappingTarget Permission.Message.Book.BookBuilder target, Permission.Message.Book book);

    Permission.Message.Bossbar mergeMessageBossbar(@MappingTarget Permission.Message.Bossbar.BossbarBuilder target, Permission.Message.Bossbar bossbar);

    Permission.Message.Brand mergeMessageBrand(@MappingTarget Permission.Message.Brand.BrandBuilder target, Permission.Message.Brand brand);

    Permission.Message.Bubble mergeMessageBubble(@MappingTarget Permission.Message.Bubble.BubbleBuilder target, Permission.Message.Bubble bubble);

    Permission.Message.Chat mergeMessageChat(@MappingTarget Permission.Message.Chat.ChatBuilder target, Permission.Message.Chat chat);

    Permission.Message.Greeting mergeMessageGreeting(@MappingTarget Permission.Message.Greeting.GreetingBuilder target, Permission.Message.Greeting greeting);

    Permission.Message.Join mergeMessageJoin(@MappingTarget Permission.Message.Join.JoinBuilder target, Permission.Message.Join join);

    Permission.Message.Quit mergeMessageQuit(@MappingTarget Permission.Message.Quit.QuitBuilder target, Permission.Message.Quit quit);

    Permission.Message.Rightclick mergeMessageRightclick(@MappingTarget Permission.Message.Rightclick.RightclickBuilder target, Permission.Message.Rightclick rightclick);

    Permission.Message.Sidebar mergeMessageSidebar(@MappingTarget Permission.Message.Sidebar.SidebarBuilder target, Permission.Message.Sidebar sidebar);

    Permission.Message.Sign mergeMessageSign(@MappingTarget Permission.Message.Sign.SignBuilder target, Permission.Message.Sign sign);

    Permission.Message.Update mergeMessageUpdate(@MappingTarget Permission.Message.Update.UpdateBuilder target, Permission.Message.Update update);

    Permission.Message.Vanilla mergeMessageVanilla(@MappingTarget Permission.Message.Vanilla.VanillaBuilder target, Permission.Message.Vanilla vanilla);

    @Mapping(target = "fcolor", expression = "java(mergeMessageFormatFColor(target.build().fcolor().toBuilder(), source.fcolor()))")
    @Mapping(target = "fixation", expression = "java(mergeMessageFormatFixation(target.build().fixation().toBuilder(), source.fixation()))")
    @Mapping(target = "image", expression = "java(mergeMessageFormatImage(target.build().image().toBuilder(), source.image()))")
    @Mapping(target = "mention", expression = "java(mergeMessageFormatMention(target.build().mention().toBuilder(), source.mention()))")
    @Mapping(target = "moderation", expression = "java(mergeMessageFormatModeration(target.build().moderation().toBuilder(), source.moderation()))")
    @Mapping(target = "names", expression = "java(mergeMessageFormatNames(target.build().names().toBuilder(), source.names()))")
    @Mapping(target = "object", expression = "java(mergeMessageFormatObject(target.build().object().toBuilder(), source.object()))")
    @Mapping(target = "questionAnswer", expression = "java(mergeMessageFormatQuestionAnswer(target.build().questionAnswer().toBuilder(), source.questionAnswer()))")
    @Mapping(target = "replacement", expression = "java(mergeMessageFormatReplacement(target.build().replacement().toBuilder(), source.replacement()))")
    @Mapping(target = "scoreboard", expression = "java(mergeMessageFormatScoreboard(target.build().scoreboard().toBuilder(), source.scoreboard()))")
    @Mapping(target = "spoiler", expression = "java(mergeMessageFormatSpoiler(target.build().spoiler().toBuilder(), source.spoiler()))")
    @Mapping(target = "translate", expression = "java(mergeMessageFormatTranslate(target.build().translate().toBuilder(), source.translate()))")
    @Mapping(target = "world", expression = "java(mergeMessageFormatWorld(target.build().world().toBuilder(), source.world()))")
    Permission.Message.Format mergeMessageFormat(@MappingTarget Permission.Message.Format.FormatBuilder target, Permission.Message.Format source);

    Permission.Message.Format.FColor mergeMessageFormatFColor(@MappingTarget Permission.Message.Format.FColor.FColorBuilder target, Permission.Message.Format.FColor fcolor);

    Permission.Message.Format.Fixation mergeMessageFormatFixation(@MappingTarget Permission.Message.Format.Fixation.FixationBuilder target, Permission.Message.Format.Fixation fixation);

    Permission.Message.Format.Image mergeMessageFormatImage(@MappingTarget Permission.Message.Format.Image.ImageBuilder target, Permission.Message.Format.Image image);

    Permission.Message.Format.Mention mergeMessageFormatMention(@MappingTarget Permission.Message.Format.Mention.MentionBuilder target, Permission.Message.Format.Mention mention);

    @Mapping(target = "caps", expression = "java(mergeMessageFormatModerationCaps(target.build().caps().toBuilder(), source.caps()))")
    @Mapping(target = "delete", expression = "java(mergeMessageFormatModerationDelete(target.build().delete().toBuilder(), source.delete()))")
    @Mapping(target = "newbie", expression = "java(mergeMessageFormatModerationNewbie(target.build().newbie().toBuilder(), source.newbie()))")
    @Mapping(target = "flood", expression = "java(mergeMessageFormatModerationFlood(target.build().flood().toBuilder(), source.flood()))")
    @Mapping(target = "swear", expression = "java(mergeMessageFormatModerationSwear(target.build().swear().toBuilder(), source.swear()))")
    Permission.Message.Format.Moderation mergeMessageFormatModeration(@MappingTarget Permission.Message.Format.Moderation.ModerationBuilder target, Permission.Message.Format.Moderation source);

    Permission.Message.Format.Moderation.Caps mergeMessageFormatModerationCaps(@MappingTarget Permission.Message.Format.Moderation.Caps.CapsBuilder target, Permission.Message.Format.Moderation.Caps caps);

    Permission.Message.Format.Moderation.Delete mergeMessageFormatModerationDelete(@MappingTarget Permission.Message.Format.Moderation.Delete.DeleteBuilder target, Permission.Message.Format.Moderation.Delete delete);

    Permission.Message.Format.Moderation.Newbie mergeMessageFormatModerationNewbie(@MappingTarget Permission.Message.Format.Moderation.Newbie.NewbieBuilder target, Permission.Message.Format.Moderation.Newbie newbie);

    Permission.Message.Format.Moderation.Flood mergeMessageFormatModerationFlood(@MappingTarget Permission.Message.Format.Moderation.Flood.FloodBuilder target, Permission.Message.Format.Moderation.Flood flood);

    Permission.Message.Format.Moderation.Swear mergeMessageFormatModerationSwear(@MappingTarget Permission.Message.Format.Moderation.Swear.SwearBuilder target, Permission.Message.Format.Moderation.Swear swear);

    Permission.Message.Format.Names mergeMessageFormatNames(@MappingTarget Permission.Message.Format.Names.NamesBuilder target, Permission.Message.Format.Names names);

    Permission.Message.Format.Object mergeMessageFormatObject(@MappingTarget Permission.Message.Format.Object.ObjectBuilder target, Permission.Message.Format.Object object);

    Permission.Message.Format.Scoreboard mergeMessageFormatScoreboard(@MappingTarget Permission.Message.Format.Scoreboard.ScoreboardBuilder target, Permission.Message.Format.Scoreboard scoreboard);

    Permission.Message.Format.QuestionAnswer mergeMessageFormatQuestionAnswer(@MappingTarget Permission.Message.Format.QuestionAnswer.QuestionAnswerBuilder target, Permission.Message.Format.QuestionAnswer questionAnswer);

    Permission.Message.Format.Replacement mergeMessageFormatReplacement(@MappingTarget Permission.Message.Format.Replacement.ReplacementBuilder target, Permission.Message.Format.Replacement replacement);

    Permission.Message.Format.Spoiler mergeMessageFormatSpoiler(@MappingTarget Permission.Message.Format.Spoiler.SpoilerBuilder target, Permission.Message.Format.Spoiler spoiler);

    Permission.Message.Format.Translate mergeMessageFormatTranslate(@MappingTarget Permission.Message.Format.Translate.TranslateBuilder target, Permission.Message.Format.Translate translate);

    Permission.Message.Format.World mergeMessageFormatWorld(@MappingTarget Permission.Message.Format.World.WorldBuilder target, Permission.Message.Format.World world);

    @Mapping(target = "belowname", expression = "java(mergeMessageObjectiveBelowname(target.build().belowname().toBuilder(), source.belowname()))")
    @Mapping(target = "tabname", expression = "java(mergeMessageObjectiveTabname(target.build().tabname().toBuilder(), source.tabname()))")
    Permission.Message.Objective mergeMessageObjective(@MappingTarget Permission.Message.Objective.ObjectiveBuilder target, Permission.Message.Objective source);

    Permission.Message.Objective.Belowname mergeMessageObjectiveBelowname(@MappingTarget Permission.Message.Objective.Belowname.BelownameBuilder target, Permission.Message.Objective.Belowname belowname);

    Permission.Message.Objective.Tabname mergeMessageObjectiveTabname(@MappingTarget Permission.Message.Objective.Tabname.TabnameBuilder target, Permission.Message.Objective.Tabname tabname);

    @Mapping(target = "icon", expression = "java(mergeMessageStatusIcon(target.build().icon().toBuilder(), source.icon()))")
    @Mapping(target = "motd", expression = "java(mergeMessageStatusMOTD(target.build().motd().toBuilder(), source.motd()))")
    @Mapping(target = "players", expression = "java(mergeMessageStatusPlayers(target.build().players().toBuilder(), source.players()))")
    @Mapping(target = "version", expression = "java(mergeMessageStatusVersion(target.build().version().toBuilder(), source.version()))")
    Permission.Message.Status mergeMessageStatus(@MappingTarget Permission.Message.Status.StatusBuilder target, Permission.Message.Status source);

    Permission.Message.Status.MOTD mergeMessageStatusMOTD(@MappingTarget Permission.Message.Status.MOTD.MOTDBuilder target, Permission.Message.Status.MOTD motd);

    Permission.Message.Status.Icon mergeMessageStatusIcon(@MappingTarget Permission.Message.Status.Icon.IconBuilder target, Permission.Message.Status.Icon icon);

    Permission.Message.Status.Players mergeMessageStatusPlayers(@MappingTarget Permission.Message.Status.Players.PlayersBuilder target, Permission.Message.Status.Players players);

    Permission.Message.Status.Version mergeMessageStatusVersion(@MappingTarget Permission.Message.Status.Version.VersionBuilder target, Permission.Message.Status.Version version);

    @Mapping(target = "footer", expression = "java(mergeMessageTabFooter(target.build().footer().toBuilder(), source.footer()))")
    @Mapping(target = "header", expression = "java(mergeMessageTabHeader(target.build().header().toBuilder(), source.header()))")
    @Mapping(target = "playerlistname", expression = "java(mergeMessageTabPlayerlistname(target.build().playerlistname().toBuilder(), source.playerlistname()))")
    Permission.Message.Tab mergeMessageTab(@MappingTarget Permission.Message.Tab.TabBuilder target, Permission.Message.Tab source);

    Permission.Message.Tab.Footer mergeMessageTabFooter(@MappingTarget Permission.Message.Tab.Footer.FooterBuilder target, Permission.Message.Tab.Footer footer);

    Permission.Message.Tab.Header mergeMessageTabHeader(@MappingTarget Permission.Message.Tab.Header.HeaderBuilder target, Permission.Message.Tab.Header header);

    Permission.Message.Tab.Playerlistname mergeMessageTabPlayerlistname(@MappingTarget Permission.Message.Tab.Playerlistname.PlayerlistnameBuilder target, Permission.Message.Tab.Playerlistname playerlistname);

}