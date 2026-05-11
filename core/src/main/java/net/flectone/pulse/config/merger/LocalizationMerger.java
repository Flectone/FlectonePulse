package net.flectone.pulse.config.merger;

import net.flectone.pulse.config.Localization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for merging {@link Localization} configuration objects.
 * <p>
 * This interface defines mapping methods for deep merging localization configurations,
 * handling nested structures through builder patterns.
 * </p>
 *
 * @author TheFaser
 * @since 1.7.1
 */
@Mapper(config = MapstructMergerConfig.class)
public interface LocalizationMerger {

    @Mapping(target = "time", expression = "java(mergeTime(target.build().time().toBuilder(), source.time()))")
    @Mapping(target = "command", expression = "java(mergeCommand(target.build().command().toBuilder(), source.command()))")
    @Mapping(target = "integration", expression = "java(mergeIntegration(target.build().integration().toBuilder(), source.integration()))")
    @Mapping(target = "message", expression = "java(mergeMessage(target.build().message().toBuilder(), source.message()))")
    Localization merge(@MappingTarget Localization.LocalizationBuilder target, Localization source);

    Localization.Time mergeTime(@MappingTarget Localization.Time.TimeBuilder target, Localization.Time source);

    @Mapping(target = "exception", expression = "java(mergeCommandException(target.build().exception().toBuilder(), source.exception()))")
    @Mapping(target = "prompt", expression = "java(mergeCommandPrompt(target.build().prompt().toBuilder(), source.prompt()))")
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
    Localization.Command mergeCommand(@MappingTarget Localization.Command.CommandBuilder target, Localization.Command source);

    Localization.Command.Exception mergeCommandException(@MappingTarget Localization.Command.Exception.ExceptionBuilder target, Localization.Command.Exception source);

    Localization.Command.Prompt mergeCommandPrompt(@MappingTarget Localization.Command.Prompt.PromptBuilder target, Localization.Command.Prompt source);

    Localization.Command.Anon mergeCommandAnon(@MappingTarget Localization.Command.Anon.AnonBuilder target, Localization.Command.Anon source);

    Localization.Command.Ball mergeCommandBall(@MappingTarget Localization.Command.Ball.BallBuilder target, Localization.Command.Ball source);

    Localization.Command.Ban mergeCommandBan(@MappingTarget Localization.Command.Ban.BanBuilder target, Localization.Command.Ban source);

    @Mapping(target = "global", expression = "java(mergeListTypeMessage(target.build().global().toBuilder(), source.global()))")
    @Mapping(target = "player", expression = "java(mergeListTypeMessage(target.build().player().toBuilder(), source.player()))")
    Localization.Command.Banlist mergeCommandBanlist(@MappingTarget Localization.Command.Banlist.BanlistBuilder target, Localization.Command.Banlist source);

    Localization.Command.Broadcast mergeCommandBroadcast(@MappingTarget Localization.Command.Broadcast.BroadcastBuilder target, Localization.Command.Broadcast source);

    Localization.Command.Chatcolor mergeCommandChatcolor(@MappingTarget Localization.Command.Chatcolor.ChatcolorBuilder target, Localization.Command.Chatcolor source);

    @Mapping(target = "checkbox", expression = "java(mergeCommandChatsettingCheckbox(target.build().checkbox().toBuilder(), source.checkbox()))")
    @Mapping(target = "menu", expression = "java(mergeCommandChatsettingMenu(target.build().menu().toBuilder(), source.menu()))")
    Localization.Command.Chatsetting mergeCommandChatsetting(@MappingTarget Localization.Command.Chatsetting.ChatsettingBuilder target, Localization.Command.Chatsetting source);

    Localization.Command.Chatsetting.Checkbox mergeCommandChatsettingCheckbox(@MappingTarget Localization.Command.Chatsetting.Checkbox.CheckboxBuilder target, Localization.Command.Chatsetting.Checkbox source);

    @Mapping(target = "chat", expression = "java(mergeCommandChatsettingMenuSubMenu(target.build().chat().toBuilder(), source.chat()))")
    @Mapping(target = "see", expression = "java(mergeCommandChatsettingMenuSubMenu(target.build().see().toBuilder(), source.see()))")
    @Mapping(target = "out", expression = "java(mergeCommandChatsettingMenuSubMenu(target.build().out().toBuilder(), source.out()))")
    Localization.Command.Chatsetting.Menu mergeCommandChatsettingMenu(@MappingTarget Localization.Command.Chatsetting.Menu.MenuBuilder target, Localization.Command.Chatsetting.Menu source);

    Localization.Command.Chatsetting.Menu.SubMenu mergeCommandChatsettingMenuSubMenu(@MappingTarget Localization.Command.Chatsetting.Menu.SubMenu.SubMenuBuilder target, Localization.Command.Chatsetting.Menu.SubMenu source);

    Localization.Command.Clearchat mergeCommandClearchat(@MappingTarget Localization.Command.Clearchat.ClearchatBuilder target, Localization.Command.Clearchat source);

    Localization.Command.Clearmail mergeCommandClearmail(@MappingTarget Localization.Command.Clearmail.ClearmailBuilder target, Localization.Command.Clearmail source);

    Localization.Command.Coin mergeCommandCoin(@MappingTarget Localization.Command.Coin.CoinBuilder target, Localization.Command.Coin source);

    Localization.Command.Deletemessage mergeCommandDeletemessage(@MappingTarget Localization.Command.Deletemessage.DeletemessageBuilder target, Localization.Command.Deletemessage source);

    Localization.Command.Dice mergeCommandDice(@MappingTarget Localization.Command.Dice.DiceBuilder target, Localization.Command.Dice source);

    Localization.Command.CommandDo mergeCommandDo(@MappingTarget Localization.Command.CommandDo.CommandDoBuilder target, Localization.Command.CommandDo source);

    Localization.Command.Emit mergeCommandEmit(@MappingTarget Localization.Command.Emit.EmitBuilder target, Localization.Command.Emit source);

    Localization.Command.Flectonepulse mergeCommandFlectonepulse(@MappingTarget Localization.Command.Flectonepulse.FlectonepulseBuilder target, Localization.Command.Flectonepulse source);

    Localization.Command.Geolocate mergeCommandGeolocate(@MappingTarget Localization.Command.Geolocate.GeolocateBuilder target, Localization.Command.Geolocate source);

    Localization.Command.Helper mergeCommandHelper(@MappingTarget Localization.Command.Helper.HelperBuilder target, Localization.Command.Helper source);

    Localization.Command.Ignore mergeCommandIgnore(@MappingTarget Localization.Command.Ignore.IgnoreBuilder target, Localization.Command.Ignore source);

    Localization.Command.Ignorelist mergeCommandIgnorelist(@MappingTarget Localization.Command.Ignorelist.IgnorelistBuilder target, Localization.Command.Ignorelist source);

    Localization.Command.Kick mergeCommandKick(@MappingTarget Localization.Command.Kick.KickBuilder target, Localization.Command.Kick source);

    Localization.Command.Mail mergeCommandMail(@MappingTarget Localization.Command.Mail.MailBuilder target, Localization.Command.Mail source);

    Localization.Command.Maintenance mergeCommandMaintenance(@MappingTarget Localization.Command.Maintenance.MaintenanceBuilder target, Localization.Command.Maintenance source);

    Localization.Command.Me mergeCommandMe(@MappingTarget Localization.Command.Me.MeBuilder target, Localization.Command.Me source);

    Localization.Command.Mute mergeCommandMute(@MappingTarget Localization.Command.Mute.MuteBuilder target, Localization.Command.Mute source);

    @Mapping(target = "global", expression = "java(mergeListTypeMessage(target.build().global().toBuilder(), source.global()))")
    @Mapping(target = "player", expression = "java(mergeListTypeMessage(target.build().player().toBuilder(), source.player()))")
    Localization.Command.Mutelist mergeCommandMutelist(@MappingTarget Localization.Command.Mutelist.MutelistBuilder target, Localization.Command.Mutelist source);

    Localization.Command.Nickname mergeCommandNickname(@MappingTarget Localization.Command.Nickname.NicknameBuilder target, Localization.Command.Nickname source);

    Localization.Command.Online mergeCommandOnline(@MappingTarget Localization.Command.Online.OnlineBuilder target, Localization.Command.Online source);

    Localization.Command.Ping mergeCommandPing(@MappingTarget Localization.Command.Ping.PingBuilder target, Localization.Command.Ping source);

    @Mapping(target = "status", expression = "java(mergeCommandPollStatus(target.build().status().toBuilder(), source.status()))")
    @Mapping(target = "modern", expression = "java(mergeCommandPollModern(target.build().modern().toBuilder(), source.modern()))")
    Localization.Command.Poll mergeCommandPoll(@MappingTarget Localization.Command.Poll.PollBuilder target, Localization.Command.Poll source);

    Localization.Command.Poll.Status mergeCommandPollStatus(@MappingTarget Localization.Command.Poll.Status.StatusBuilder target, Localization.Command.Poll.Status source);

    Localization.Command.Poll.Modern mergeCommandPollModern(@MappingTarget Localization.Command.Poll.Modern.ModernBuilder target, Localization.Command.Poll.Modern source);

    Localization.Command.Reply mergeCommandReply(@MappingTarget Localization.Command.Reply.ReplyBuilder target, Localization.Command.Reply source);

    Localization.Command.Rockpaperscissors mergeCommandRockpaperscissors(@MappingTarget Localization.Command.Rockpaperscissors.RockpaperscissorsBuilder target, Localization.Command.Rockpaperscissors source);

    Localization.Command.Sprite mergeCommandSprite(@MappingTarget Localization.Command.Sprite.SpriteBuilder target, Localization.Command.Sprite source);

    Localization.Command.Spy mergeCommandSpy(@MappingTarget Localization.Command.Spy.SpyBuilder target, Localization.Command.Spy source);

    Localization.Command.Stream mergeCommandStream(@MappingTarget Localization.Command.Stream.StreamBuilder target, Localization.Command.Stream source);

    Localization.Command.Symbol mergeCommandSymbol(@MappingTarget Localization.Command.Symbol.SymbolBuilder target, Localization.Command.Symbol source);

    Localization.Command.Tell mergeCommandTell(@MappingTarget Localization.Command.Tell.TellBuilder target, Localization.Command.Tell source);

    @Mapping(target = "symbol", expression = "java(mergeCommandTictactoeSymbol(target.build().symbol().toBuilder(), source.symbol()))")
    Localization.Command.Tictactoe mergeCommandTictactoe(@MappingTarget Localization.Command.Tictactoe.TictactoeBuilder target, Localization.Command.Tictactoe source);

    Localization.Command.Tictactoe.Symbol mergeCommandTictactoeSymbol(@MappingTarget Localization.Command.Tictactoe.Symbol.SymbolBuilder target, Localization.Command.Tictactoe.Symbol source);

    Localization.Command.Toponline mergeCommandToponline(@MappingTarget Localization.Command.Toponline.ToponlineBuilder target, Localization.Command.Toponline source);

    Localization.Command.Translateto mergeCommandTranslateto(@MappingTarget Localization.Command.Translateto.TranslatetoBuilder target, Localization.Command.Translateto source);

    Localization.Command.CommandTry mergeCommandTry(@MappingTarget Localization.Command.CommandTry.CommandTryBuilder target, Localization.Command.CommandTry source);

    Localization.Command.Unban mergeCommandUnban(@MappingTarget Localization.Command.Unban.UnbanBuilder target, Localization.Command.Unban source);

    Localization.Command.Unmute mergeCommandUnmute(@MappingTarget Localization.Command.Unmute.UnmuteBuilder target, Localization.Command.Unmute source);

    Localization.Command.Unwarn mergeCommandUnwarn(@MappingTarget Localization.Command.Unwarn.UnwarnBuilder target, Localization.Command.Unwarn source);

    Localization.Command.Warn mergeCommandWarn(@MappingTarget Localization.Command.Warn.WarnBuilder target, Localization.Command.Warn source);

    @Mapping(target = "global", expression = "java(mergeListTypeMessage(target.build().global().toBuilder(), source.global()))")
    @Mapping(target = "player", expression = "java(mergeListTypeMessage(target.build().player().toBuilder(), source.player()))")
    Localization.Command.Warnlist mergeCommandWarnlist(@MappingTarget Localization.Command.Warnlist.WarnlistBuilder target, Localization.Command.Warnlist source);

    @Mapping(target = "global", expression = "java(mergeListTypeMessage(target.build().global().toBuilder(), source.global()))")
    @Mapping(target = "player", expression = "java(mergeListTypeMessage(target.build().player().toBuilder(), source.player()))")
    Localization.Command.Whitelist mergeCommandWhitelist(@MappingTarget Localization.Command.Whitelist.WhitelistBuilder target, Localization.Command.Whitelist source);

    @Mapping(target = "discord", expression = "java(mergeIntegrationDiscord(target.build().discord().toBuilder(), source.discord()))")
    @Mapping(target = "telegram", expression = "java(mergeIntegrationTelegram(target.build().telegram().toBuilder(), source.telegram()))")
    @Mapping(target = "twitch", expression = "java(mergeIntegrationTwitch(target.build().twitch().toBuilder(), source.twitch()))")
    Localization.Integration mergeIntegration(@MappingTarget Localization.Integration.IntegrationBuilder target, Localization.Integration source);

    Localization.Integration.Discord mergeIntegrationDiscord(@MappingTarget Localization.Integration.Discord.DiscordBuilder target, Localization.Integration.Discord source);

    Localization.Integration.Telegram mergeIntegrationTelegram(@MappingTarget Localization.Integration.Telegram.TelegramBuilder target, Localization.Integration.Telegram source);

    Localization.Integration.Twitch mergeIntegrationTwitch(@MappingTarget Localization.Integration.Twitch.TwitchBuilder target, Localization.Integration.Twitch source);

    @Mapping(target = "afk", expression = "java(mergeMessageAfk(target.build().afk().toBuilder(), source.afk()))")
    @Mapping(target = "auto", expression = "java(mergeMessageAuto(target.build().auto().toBuilder(), source.auto()))")
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
    @Mapping(target = "status", expression = "java(mergeMessageStatus(target.build().status().toBuilder(), source.status()))")
    @Mapping(target = "tab", expression = "java(mergeMessageTab(target.build().tab().toBuilder(), source.tab()))")
    @Mapping(target = "update", expression = "java(mergeMessageUpdate(target.build().update().toBuilder(), source.update()))")
    @Mapping(target = "vanilla", expression = "java(mergeMessageVanilla(target.build().vanilla().toBuilder(), source.vanilla()))")
    Localization.Message mergeMessage(@MappingTarget Localization.Message.MessageBuilder target, Localization.Message source);

    Localization.Message.Afk mergeMessageAfk(@MappingTarget Localization.Message.Afk.AfkBuilder target, Localization.Message.Afk source);

    Localization.Message.Auto mergeMessageAuto(@MappingTarget Localization.Message.Auto.AutoBuilder target, Localization.Message.Auto source);

    Localization.Message.Bossbar mergeMessageBossbar(@MappingTarget Localization.Message.Bossbar.BossbarBuilder target, Localization.Message.Bossbar source);

    Localization.Message.Brand mergeMessageBrand(@MappingTarget Localization.Message.Brand.BrandBuilder target, Localization.Message.Brand source);

    Localization.Message.Bubble mergeMessageBubble(@MappingTarget Localization.Message.Bubble.BubbleBuilder target, Localization.Message.Bubble source);

    Localization.Message.Chat mergeMessageChat(@MappingTarget Localization.Message.Chat.ChatBuilder target, Localization.Message.Chat source);

    @Mapping(target = "animation", expression = "java(mergeMessageFormatAnimation(target.build().animation().toBuilder(), source.animation()))")
    @Mapping(target = "condition", expression = "java(mergeMessageFormatCondition(target.build().condition().toBuilder(), source.condition()))")
    @Mapping(target = "object", expression = "java(mergeMessageFormatObject(target.build().object().toBuilder(), source.object()))")
    @Mapping(target = "replacement", expression = "java(mergeMessageFormatReplacement(target.build().replacement().toBuilder(), source.replacement()))")
    @Mapping(target = "mention", expression = "java(mergeMessageFormatMention(target.build().mention().toBuilder(), source.mention()))")
    @Mapping(target = "moderation", expression = "java(mergeMessageFormatModeration(target.build().moderation().toBuilder(), source.moderation()))")
    @Mapping(target = "names", expression = "java(mergeMessageFormatNames(target.build().names().toBuilder(), source.names()))")
    @Mapping(target = "questionAnswer", expression = "java(mergeMessageFormatQuestionAnswer(target.build().questionAnswer().toBuilder(), source.questionAnswer()))")
    @Mapping(target = "translate", expression = "java(mergeMessageFormatTranslate(target.build().translate().toBuilder(), source.translate()))")
    Localization.Message.Format mergeMessageFormat(@MappingTarget Localization.Message.Format.FormatBuilder target, Localization.Message.Format source);

    Localization.Message.Format.Animation mergeMessageFormatAnimation(@MappingTarget Localization.Message.Format.Animation.AnimationBuilder target, Localization.Message.Format.Animation source);

    Localization.Message.Format.Condition mergeMessageFormatCondition(@MappingTarget Localization.Message.Format.Condition.ConditionBuilder target, Localization.Message.Format.Condition source);

    Localization.Message.Format.Object mergeMessageFormatObject(@MappingTarget Localization.Message.Format.Object.ObjectBuilder target, Localization.Message.Format.Object source);

    Localization.Message.Format.Replacement mergeMessageFormatReplacement(@MappingTarget Localization.Message.Format.Replacement.ReplacementBuilder target, Localization.Message.Format.Replacement source);

    Localization.Message.Format.Mention mergeMessageFormatMention(@MappingTarget Localization.Message.Format.Mention.MentionBuilder target, Localization.Message.Format.Mention source);

    @Mapping(target = "caps", expression = "java(mergeMessageFormatModerationCaps(target.build().caps().toBuilder(), source.caps()))")
    @Mapping(target = "delete", expression = "java(mergeMessageFormatModerationDelete(target.build().delete().toBuilder(), source.delete()))")
    @Mapping(target = "flood", expression = "java(mergeMessageFormatModerationFlood(target.build().flood().toBuilder(), source.flood()))")
    @Mapping(target = "newbie", expression = "java(mergeMessageFormatModerationNewbie(target.build().newbie().toBuilder(), source.newbie()))")
    @Mapping(target = "swear", expression = "java(mergeMessageFormatModerationSwear(target.build().swear().toBuilder(), source.swear()))")
    Localization.Message.Format.Moderation mergeMessageFormatModeration(@MappingTarget Localization.Message.Format.Moderation.ModerationBuilder target, Localization.Message.Format.Moderation source);

    Localization.Message.Format.Moderation.Caps mergeMessageFormatModerationCaps(@MappingTarget Localization.Message.Format.Moderation.Caps.CapsBuilder target, Localization.Message.Format.Moderation.Caps source);

    Localization.Message.Format.Moderation.Delete mergeMessageFormatModerationDelete(@MappingTarget Localization.Message.Format.Moderation.Delete.DeleteBuilder target, Localization.Message.Format.Moderation.Delete source);

    Localization.Message.Format.Moderation.Flood mergeMessageFormatModerationFlood(@MappingTarget Localization.Message.Format.Moderation.Flood.FloodBuilder target, Localization.Message.Format.Moderation.Flood source);

    Localization.Message.Format.Moderation.Newbie mergeMessageFormatModerationNewbie(@MappingTarget Localization.Message.Format.Moderation.Newbie.NewbieBuilder target, Localization.Message.Format.Moderation.Newbie source);

    Localization.Message.Format.Moderation.Swear mergeMessageFormatModerationSwear(@MappingTarget Localization.Message.Format.Moderation.Swear.SwearBuilder target, Localization.Message.Format.Moderation.Swear source);

    Localization.Message.Format.Names mergeMessageFormatNames(@MappingTarget Localization.Message.Format.Names.NamesBuilder target, Localization.Message.Format.Names source);

    Localization.Message.Format.QuestionAnswer mergeMessageFormatQuestionAnswer(@MappingTarget Localization.Message.Format.QuestionAnswer.QuestionAnswerBuilder target, Localization.Message.Format.QuestionAnswer source);

    Localization.Message.Format.Translate mergeMessageFormatTranslate(@MappingTarget Localization.Message.Format.Translate.TranslateBuilder target, Localization.Message.Format.Translate source);

    Localization.Message.Greeting mergeMessageGreeting(@MappingTarget Localization.Message.Greeting.GreetingBuilder target, Localization.Message.Greeting source);

    Localization.Message.Join mergeMessageJoin(@MappingTarget Localization.Message.Join.JoinBuilder target, Localization.Message.Join source);

    Localization.Message.Quit mergeMessageQuit(@MappingTarget Localization.Message.Quit.QuitBuilder target, Localization.Message.Quit source);

    Localization.Message.Rightclick mergeMessageRightclick(@MappingTarget Localization.Message.Rightclick.RightclickBuilder target, Localization.Message.Rightclick source);

    @Mapping(target = "objective", expression = "java(mergeMessageScoreboardObjective(target.build().objective().toBuilder(), source.objective()))")
    Localization.Message.Scoreboard mergeMessageScoreboard(@MappingTarget Localization.Message.Scoreboard.ScoreboardBuilder target, Localization.Message.Scoreboard source);

    @Mapping(target = "belowname", expression = "java(mergeMessageScoreboardObjectiveBelowname(target.build().belowname().toBuilder(), source.belowname()))")
    @Mapping(target = "tabname", expression = "java(mergeMessageScoreboardObjectiveTabname(target.build().tabname().toBuilder(), source.tabname()))")
    Localization.Message.Scoreboard.Objective mergeMessageScoreboardObjective(@MappingTarget Localization.Message.Scoreboard.Objective.ObjectiveBuilder target, Localization.Message.Scoreboard.Objective source);

    Localization.Message.Scoreboard.Objective.Belowname mergeMessageScoreboardObjectiveBelowname(@MappingTarget Localization.Message.Scoreboard.Objective.Belowname.BelownameBuilder target, Localization.Message.Scoreboard.Objective.Belowname source);

    Localization.Message.Scoreboard.Objective.Tabname mergeMessageScoreboardObjectiveTabname(@MappingTarget Localization.Message.Scoreboard.Objective.Tabname.TabnameBuilder target, Localization.Message.Scoreboard.Objective.Tabname source);

    Localization.Message.Serverlink mergeMessageServerlink(@MappingTarget Localization.Message.Serverlink.ServerlinkBuilder target, Localization.Message.Serverlink source);

    Localization.Message.Sidebar mergeMessageSidebar(@MappingTarget Localization.Message.Sidebar.SidebarBuilder target, Localization.Message.Sidebar source);

    @Mapping(target = "motd", expression = "java(mergeMessageStatusMOTD(target.build().motd().toBuilder(), source.motd()))")
    @Mapping(target = "players", expression = "java(mergeMessageStatusPlayers(target.build().players().toBuilder(), source.players()))")
    @Mapping(target = "version", expression = "java(mergeMessageStatusVersion(target.build().version().toBuilder(), source.version()))")
    Localization.Message.Status mergeMessageStatus(@MappingTarget Localization.Message.Status.StatusBuilder target, Localization.Message.Status source);

    Localization.Message.Status.MOTD mergeMessageStatusMOTD(@MappingTarget Localization.Message.Status.MOTD.MOTDBuilder target, Localization.Message.Status.MOTD source);

    Localization.Message.Status.Players mergeMessageStatusPlayers(@MappingTarget Localization.Message.Status.Players.PlayersBuilder target, Localization.Message.Status.Players source);

    Localization.Message.Status.Version mergeMessageStatusVersion(@MappingTarget Localization.Message.Status.Version.VersionBuilder target, Localization.Message.Status.Version source);

    @Mapping(target = "header", expression = "java(mergeMessageTabHeader(target.build().header().toBuilder(), source.header()))")
    @Mapping(target = "footer", expression = "java(mergeMessageTabFooter(target.build().footer().toBuilder(), source.footer()))")
    @Mapping(target = "playerlistname", expression = "java(mergeMessageTabPlayerlistname(target.build().playerlistname().toBuilder(), source.playerlistname()))")
    Localization.Message.Tab mergeMessageTab(@MappingTarget Localization.Message.Tab.TabBuilder target, Localization.Message.Tab source);

    Localization.Message.Tab.Header mergeMessageTabHeader(@MappingTarget Localization.Message.Tab.Header.HeaderBuilder target, Localization.Message.Tab.Header source);

    Localization.Message.Tab.Footer mergeMessageTabFooter(@MappingTarget Localization.Message.Tab.Footer.FooterBuilder target, Localization.Message.Tab.Footer source);

    Localization.Message.Tab.Playerlistname mergeMessageTabPlayerlistname(@MappingTarget Localization.Message.Tab.Playerlistname.PlayerlistnameBuilder target, Localization.Message.Tab.Playerlistname source);

    Localization.Message.Update mergeMessageUpdate(@MappingTarget Localization.Message.Update.UpdateBuilder target, Localization.Message.Update source);

    Localization.Message.Vanilla mergeMessageVanilla(@MappingTarget Localization.Message.Vanilla.VanillaBuilder target, Localization.Message.Vanilla source);

    Localization.ListTypeMessage mergeListTypeMessage(@MappingTarget Localization.ListTypeMessage.ListTypeMessageBuilder target, Localization.ListTypeMessage source);

}