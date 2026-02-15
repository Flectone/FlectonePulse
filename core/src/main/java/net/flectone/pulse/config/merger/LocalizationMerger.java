package net.flectone.pulse.config.merger;

import net.flectone.pulse.config.Localization;
import org.mapstruct.*;

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

    Localization.Time mergeTime(@MappingTarget Localization.Time.TimeBuilder target, Localization.Time time);

    @Mapping(target = "exception", expression = "java(mergeException(target.build().exception().toBuilder(), source.exception()))")
    @Mapping(target = "prompt", expression = "java(mergePrompt(target.build().prompt().toBuilder(), source.prompt()))")
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
    @Mapping(target = "nickname", expression = "java(mergeNickname(target.build().nickname().toBuilder(), source.nickname()))")
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
    Localization.Command mergeCommand(@MappingTarget Localization.Command.CommandBuilder target, Localization.Command source);

    Localization.Command.Exception mergeException(@MappingTarget Localization.Command.Exception.ExceptionBuilder target, Localization.Command.Exception exception);

    Localization.Command.Prompt mergePrompt(@MappingTarget Localization.Command.Prompt.PromptBuilder target, Localization.Command.Prompt prompt);

    Localization.Command.Anon mergeAnon(@MappingTarget Localization.Command.Anon.AnonBuilder target, Localization.Command.Anon anon);

    Localization.Command.Ball mergeBall(@MappingTarget Localization.Command.Ball.BallBuilder target, Localization.Command.Ball ball);

    Localization.Command.Ban mergeBan(@MappingTarget Localization.Command.Ban.BanBuilder target, Localization.Command.Ban ban);

    @Mapping(target = "global", expression = "java(mergeListTypeMessage(target.build().global().toBuilder(), banlist.global()))")
    @Mapping(target = "player", expression = "java(mergeListTypeMessage(target.build().player().toBuilder(), banlist.player()))")
    Localization.Command.Banlist mergeBanlist(@MappingTarget Localization.Command.Banlist.BanlistBuilder target, Localization.Command.Banlist banlist);

    Localization.Command.Broadcast mergeBroadcast(@MappingTarget Localization.Command.Broadcast.BroadcastBuilder target, Localization.Command.Broadcast broadcast);

    Localization.Command.Chatcolor mergeChatcolor(@MappingTarget Localization.Command.Chatcolor.ChatcolorBuilder target, Localization.Command.Chatcolor chatcolor);

    @Mapping(target = "checkbox", expression = "java(mergeCheckbox(target.build().checkbox().toBuilder(), source.checkbox()))")
    @Mapping(target = "menu", expression = "java(mergeMenu(target.build().menu().toBuilder(), source.menu()))")
    Localization.Command.Chatsetting mergeChatsetting(@MappingTarget Localization.Command.Chatsetting.ChatsettingBuilder target, Localization.Command.Chatsetting source);

    Localization.Command.Chatsetting.Checkbox mergeCheckbox(@MappingTarget Localization.Command.Chatsetting.Checkbox.CheckboxBuilder target, Localization.Command.Chatsetting.Checkbox checkbox);

    @Mapping(target = "chat", expression = "java(mergeSubMenu(target.build().chat().toBuilder(), source.chat()))")
    @Mapping(target = "see", expression = "java(mergeSubMenu(target.build().see().toBuilder(), source.see()))")
    @Mapping(target = "out", expression = "java(mergeSubMenu(target.build().out().toBuilder(), source.out()))")
    Localization.Command.Chatsetting.Menu mergeMenu(@MappingTarget Localization.Command.Chatsetting.Menu.MenuBuilder target, Localization.Command.Chatsetting.Menu source);

    Localization.Command.Chatsetting.Menu.SubMenu mergeSubMenu(@MappingTarget Localization.Command.Chatsetting.Menu.SubMenu.SubMenuBuilder target, Localization.Command.Chatsetting.Menu.SubMenu subMenu);

    Localization.Command.Clearchat mergeClearchat(@MappingTarget Localization.Command.Clearchat.ClearchatBuilder target, Localization.Command.Clearchat clearchat);

    Localization.Command.Clearmail mergeClearmail(@MappingTarget Localization.Command.Clearmail.ClearmailBuilder target, Localization.Command.Clearmail clearmail);

    Localization.Command.Coin mergeCoin(@MappingTarget Localization.Command.Coin.CoinBuilder target, Localization.Command.Coin coin);

    Localization.Command.Deletemessage mergeDeletemessage(@MappingTarget Localization.Command.Deletemessage.DeletemessageBuilder target, Localization.Command.Deletemessage deletemessage);

    Localization.Command.Dice mergeDice(@MappingTarget Localization.Command.Dice.DiceBuilder target, Localization.Command.Dice dice);

    Localization.Command.CommandDo mergeCommandDo(@MappingTarget Localization.Command.CommandDo.CommandDoBuilder target, Localization.Command.CommandDo commandDo);

    Localization.Command.Emit mergeEmit(@MappingTarget Localization.Command.Emit.EmitBuilder target, Localization.Command.Emit emit);

    Localization.Command.Flectonepulse mergeFlectonepulse(@MappingTarget Localization.Command.Flectonepulse.FlectonepulseBuilder target, Localization.Command.Flectonepulse flectonepulse);

    Localization.Command.Geolocate mergeGeolocate(@MappingTarget Localization.Command.Geolocate.GeolocateBuilder target, Localization.Command.Geolocate geolocate);

    Localization.Command.Helper mergeHelper(@MappingTarget Localization.Command.Helper.HelperBuilder target, Localization.Command.Helper helper);

    Localization.Command.Ignore mergeIgnore(@MappingTarget Localization.Command.Ignore.IgnoreBuilder target, Localization.Command.Ignore ignore);

    Localization.Command.Ignorelist mergeIgnorelist(@MappingTarget Localization.Command.Ignorelist.IgnorelistBuilder target, Localization.Command.Ignorelist ignorelist);

    Localization.Command.Kick mergeKick(@MappingTarget Localization.Command.Kick.KickBuilder target, Localization.Command.Kick kick);

    Localization.Command.Mail mergeMail(@MappingTarget Localization.Command.Mail.MailBuilder target, Localization.Command.Mail mail);

    Localization.Command.Maintenance mergeMaintenance(@MappingTarget Localization.Command.Maintenance.MaintenanceBuilder target, Localization.Command.Maintenance maintenance);

    Localization.Command.Me mergeMe(@MappingTarget Localization.Command.Me.MeBuilder target, Localization.Command.Me me);

    Localization.Command.Mute mergeMute(@MappingTarget Localization.Command.Mute.MuteBuilder target, Localization.Command.Mute mute);

    @Mapping(target = "global", expression = "java(mergeListTypeMessage(target.build().global().toBuilder(), mutelist.global()))")
    @Mapping(target = "player", expression = "java(mergeListTypeMessage(target.build().player().toBuilder(), mutelist.player()))")
    Localization.Command.Mutelist mergeMutelist(@MappingTarget Localization.Command.Mutelist.MutelistBuilder target, Localization.Command.Mutelist mutelist);

    Localization.Command.Nickname mergeNickname(@MappingTarget Localization.Command.Nickname.NicknameBuilder target, Localization.Command.Nickname nickname);

    Localization.Command.Online mergeOnline(@MappingTarget Localization.Command.Online.OnlineBuilder target, Localization.Command.Online online);

    Localization.Command.Ping mergePing(@MappingTarget Localization.Command.Ping.PingBuilder target, Localization.Command.Ping ping);

    @Mapping(target = "status", expression = "java(mergeStatus(target.build().status().toBuilder(), source.status()))")
    @Mapping(target = "modern", expression = "java(mergeModern(target.build().modern().toBuilder(), source.modern()))")
    Localization.Command.Poll mergePoll(@MappingTarget Localization.Command.Poll.PollBuilder target, Localization.Command.Poll source);

    Localization.Command.Poll.Status mergeStatus(@MappingTarget Localization.Command.Poll.Status.StatusBuilder target, Localization.Command.Poll.Status status);

    Localization.Command.Poll.Modern mergeModern(@MappingTarget Localization.Command.Poll.Modern.ModernBuilder target, Localization.Command.Poll.Modern modern);

    Localization.Command.Reply mergeReply(@MappingTarget Localization.Command.Reply.ReplyBuilder target, Localization.Command.Reply reply);

    Localization.Command.Rockpaperscissors mergeRockpaperscissors(@MappingTarget Localization.Command.Rockpaperscissors.RockpaperscissorsBuilder target, Localization.Command.Rockpaperscissors rockpaperscissors);

    Localization.Command.Sprite mergeSprite(@MappingTarget Localization.Command.Sprite.SpriteBuilder target, Localization.Command.Sprite sprite);

    Localization.Command.Spy mergeSpy(@MappingTarget Localization.Command.Spy.SpyBuilder target, Localization.Command.Spy spy);

    Localization.Command.Stream mergeStream(@MappingTarget Localization.Command.Stream.StreamBuilder target, Localization.Command.Stream stream);

    Localization.Command.Symbol mergeSymbol(@MappingTarget Localization.Command.Symbol.SymbolBuilder target, Localization.Command.Symbol symbol);

    Localization.Command.Tell mergeTell(@MappingTarget Localization.Command.Tell.TellBuilder target, Localization.Command.Tell tell);

    @Mapping(target = "symbol", expression = "java(mergeSymbol2(target.build().symbol().toBuilder(), source.symbol()))")
    Localization.Command.Tictactoe mergeTictactoe(@MappingTarget Localization.Command.Tictactoe.TictactoeBuilder target, Localization.Command.Tictactoe source);

    Localization.Command.Tictactoe.Symbol mergeSymbol2(@MappingTarget Localization.Command.Tictactoe.Symbol.SymbolBuilder target, Localization.Command.Tictactoe.Symbol symbol);

    Localization.Command.Toponline mergeToponline(@MappingTarget Localization.Command.Toponline.ToponlineBuilder target, Localization.Command.Toponline toponline);

    Localization.Command.Translateto mergeTranslateto(@MappingTarget Localization.Command.Translateto.TranslatetoBuilder target, Localization.Command.Translateto translateto);

    Localization.Command.CommandTry mergeCommandTry(@MappingTarget Localization.Command.CommandTry.CommandTryBuilder target, Localization.Command.CommandTry commandTry);

    Localization.Command.Unban mergeUnban(@MappingTarget Localization.Command.Unban.UnbanBuilder target, Localization.Command.Unban unban);

    Localization.Command.Unmute mergeUnmute(@MappingTarget Localization.Command.Unmute.UnmuteBuilder target, Localization.Command.Unmute unmute);

    Localization.Command.Unwarn mergeUnwarn(@MappingTarget Localization.Command.Unwarn.UnwarnBuilder target, Localization.Command.Unwarn unwarn);

    Localization.Command.Warn mergeWarn(@MappingTarget Localization.Command.Warn.WarnBuilder target, Localization.Command.Warn warn);

    @Mapping(target = "global", expression = "java(mergeListTypeMessage(target.build().global().toBuilder(), warnlist.global()))")
    @Mapping(target = "player", expression = "java(mergeListTypeMessage(target.build().player().toBuilder(), warnlist.player()))")
    Localization.Command.Warnlist mergeWarnlist(@MappingTarget Localization.Command.Warnlist.WarnlistBuilder target, Localization.Command.Warnlist warnlist);

    @Mapping(target = "discord", expression = "java(mergeDiscord(target.build().discord().toBuilder(), source.discord()))")
    @Mapping(target = "telegram", expression = "java(mergeTelegram(target.build().telegram().toBuilder(), source.telegram()))")
    @Mapping(target = "twitch", expression = "java(mergeTwitch(target.build().twitch().toBuilder(), source.twitch()))")
    Localization.Integration mergeIntegration(@MappingTarget Localization.Integration.IntegrationBuilder target, Localization.Integration source);

    Localization.Integration.Discord mergeDiscord(@MappingTarget Localization.Integration.Discord.DiscordBuilder target, Localization.Integration.Discord discord);

    Localization.Integration.Telegram mergeTelegram(@MappingTarget Localization.Integration.Telegram.TelegramBuilder target, Localization.Integration.Telegram telegram);

    Localization.Integration.Twitch mergeTwitch(@MappingTarget Localization.Integration.Twitch.TwitchBuilder target, Localization.Integration.Twitch twitch);

    @Mapping(target = "afk", expression = "java(mergeAfk2(target.build().afk().toBuilder(), source.afk()))")
    @Mapping(target = "auto", expression = "java(mergeAuto(target.build().auto().toBuilder(), source.auto()))")
    @Mapping(target = "bossbar", expression = "java(mergeBossbar(target.build().bossbar().toBuilder(), source.bossbar()))")
    @Mapping(target = "brand", expression = "java(mergeBrand(target.build().brand().toBuilder(), source.brand()))")
    @Mapping(target = "bubble", expression = "java(mergeBubble(target.build().bubble().toBuilder(), source.bubble()))")
    @Mapping(target = "chat", expression = "java(mergeChat2(target.build().chat().toBuilder(), source.chat()))")
    @Mapping(target = "format", expression = "java(mergeFormat(target.build().format().toBuilder(), source.format()))")
    @Mapping(target = "greeting", expression = "java(mergeGreeting(target.build().greeting().toBuilder(), source.greeting()))")
    @Mapping(target = "join", expression = "java(mergeJoin(target.build().join().toBuilder(), source.join()))")
    @Mapping(target = "objective", expression = "java(mergeObjective(target.build().objective().toBuilder(), source.objective()))")
    @Mapping(target = "quit", expression = "java(mergeQuit(target.build().quit().toBuilder(), source.quit()))")
    @Mapping(target = "rightclick", expression = "java(mergeRightclick(target.build().rightclick().toBuilder(), source.rightclick()))")
    @Mapping(target = "sidebar", expression = "java(mergeSidebar(target.build().sidebar().toBuilder(), source.sidebar()))")
    @Mapping(target = "status", expression = "java(mergeStatus2(target.build().status().toBuilder(), source.status()))")
    @Mapping(target = "tab", expression = "java(mergeTab(target.build().tab().toBuilder(), source.tab()))")
    @Mapping(target = "update", expression = "java(mergeUpdate(target.build().update().toBuilder(), source.update()))")
    @Mapping(target = "vanilla", expression = "java(mergeVanilla(target.build().vanilla().toBuilder(), source.vanilla()))")
    Localization.Message mergeMessage(@MappingTarget Localization.Message.MessageBuilder target, Localization.Message source);

    Localization.Message.Afk mergeAfk2(@MappingTarget Localization.Message.Afk.AfkBuilder target, Localization.Message.Afk afk);

    Localization.Message.Auto mergeAuto(@MappingTarget Localization.Message.Auto.AutoBuilder target, Localization.Message.Auto auto);

    Localization.Message.Bossbar mergeBossbar(@MappingTarget Localization.Message.Bossbar.BossbarBuilder target, Localization.Message.Bossbar bossbar);

    Localization.Message.Brand mergeBrand(@MappingTarget Localization.Message.Brand.BrandBuilder target, Localization.Message.Brand brand);

    Localization.Message.Bubble mergeBubble(@MappingTarget Localization.Message.Bubble.BubbleBuilder target, Localization.Message.Bubble bubble);

    Localization.Message.Chat mergeChat2(@MappingTarget Localization.Message.Chat.ChatBuilder target, Localization.Message.Chat chat);

    @Mapping(target = "animation", expression = "java(mergeAnimation(target.build().animation().toBuilder(), source.animation()))")
    @Mapping(target = "object", expression = "java(mergeObject(target.build().object().toBuilder(), source.object()))")
    @Mapping(target = "replacement", expression = "java(mergeReplacement(target.build().replacement().toBuilder(), source.replacement()))")
    @Mapping(target = "mention", expression = "java(mergeMention2(target.build().mention().toBuilder(), source.mention()))")
    @Mapping(target = "moderation", expression = "java(mergeModeration(target.build().moderation().toBuilder(), source.moderation()))")
    @Mapping(target = "names", expression = "java(mergeNames(target.build().names().toBuilder(), source.names()))")
    @Mapping(target = "questionAnswer", expression = "java(mergeQuestionAnswer(target.build().questionAnswer().toBuilder(), source.questionAnswer()))")
    @Mapping(target = "translate", expression = "java(mergeTranslate(target.build().translate().toBuilder(), source.translate()))")
    Localization.Message.Format mergeFormat(@MappingTarget Localization.Message.Format.FormatBuilder target, Localization.Message.Format source);

    Localization.Message.Format.Animation mergeAnimation(@MappingTarget Localization.Message.Format.Animation.AnimationBuilder target, Localization.Message.Format.Animation animation);

    Localization.Message.Format.Object mergeObject(@MappingTarget Localization.Message.Format.Object.ObjectBuilder target, Localization.Message.Format.Object object);

    Localization.Message.Format.Replacement mergeReplacement(@MappingTarget Localization.Message.Format.Replacement.ReplacementBuilder target, Localization.Message.Format.Replacement replacement);

    Localization.Message.Format.Mention mergeMention2(@MappingTarget Localization.Message.Format.Mention.MentionBuilder target, Localization.Message.Format.Mention mention);

    @Mapping(target = "delete", expression = "java(mergeDelete(target.build().delete().toBuilder(), source.delete()))")
    @Mapping(target = "newbie", expression = "java(mergeNewbie(target.build().newbie().toBuilder(), source.newbie()))")
    @Mapping(target = "swear", expression = "java(mergeSwear(target.build().swear().toBuilder(), source.swear()))")
    Localization.Message.Format.Moderation mergeModeration(@MappingTarget Localization.Message.Format.Moderation.ModerationBuilder target, Localization.Message.Format.Moderation source);

    Localization.Message.Format.Moderation.Delete mergeDelete(@MappingTarget Localization.Message.Format.Moderation.Delete.DeleteBuilder target, Localization.Message.Format.Moderation.Delete delete);

    Localization.Message.Format.Moderation.Newbie mergeNewbie(@MappingTarget Localization.Message.Format.Moderation.Newbie.NewbieBuilder target, Localization.Message.Format.Moderation.Newbie newbie);

    Localization.Message.Format.Moderation.Swear mergeSwear(@MappingTarget Localization.Message.Format.Moderation.Swear.SwearBuilder target, Localization.Message.Format.Moderation.Swear swear);

    Localization.Message.Format.Names mergeNames(@MappingTarget Localization.Message.Format.Names.NamesBuilder target, Localization.Message.Format.Names names);

    Localization.Message.Format.QuestionAnswer mergeQuestionAnswer(@MappingTarget Localization.Message.Format.QuestionAnswer.QuestionAnswerBuilder target, Localization.Message.Format.QuestionAnswer questionAnswer);

    Localization.Message.Format.Translate mergeTranslate(@MappingTarget Localization.Message.Format.Translate.TranslateBuilder target, Localization.Message.Format.Translate translate);

    Localization.Message.Greeting mergeGreeting(@MappingTarget Localization.Message.Greeting.GreetingBuilder target, Localization.Message.Greeting greeting);

    Localization.Message.Join mergeJoin(@MappingTarget Localization.Message.Join.JoinBuilder target, Localization.Message.Join join);

    @Mapping(target = "belowname", expression = "java(mergeBelowname(target.build().belowname().toBuilder(), source.belowname()))")
    @Mapping(target = "tabname", expression = "java(mergeTabname(target.build().tabname().toBuilder(), source.tabname()))")
    Localization.Message.Objective mergeObjective(@MappingTarget Localization.Message.Objective.ObjectiveBuilder target, Localization.Message.Objective source);

    Localization.Message.Objective.Belowname mergeBelowname(@MappingTarget Localization.Message.Objective.Belowname.BelownameBuilder target, Localization.Message.Objective.Belowname belowname);

    Localization.Message.Objective.Tabname mergeTabname(@MappingTarget Localization.Message.Objective.Tabname.TabnameBuilder target, Localization.Message.Objective.Tabname tabname);

    Localization.Message.Quit mergeQuit(@MappingTarget Localization.Message.Quit.QuitBuilder target, Localization.Message.Quit quit);

    Localization.Message.Rightclick mergeRightclick(@MappingTarget Localization.Message.Rightclick.RightclickBuilder target, Localization.Message.Rightclick rightclick);

    Localization.Message.Sidebar mergeSidebar(@MappingTarget Localization.Message.Sidebar.SidebarBuilder target, Localization.Message.Sidebar sidebar);

    @Mapping(target = "motd", expression = "java(mergeMOTD2(target.build().motd().toBuilder(), source.motd()))")
    @Mapping(target = "players", expression = "java(mergePlayers(target.build().players().toBuilder(), source.players()))")
    @Mapping(target = "version", expression = "java(mergeVersion(target.build().version().toBuilder(), source.version()))")
    Localization.Message.Status mergeStatus2(@MappingTarget Localization.Message.Status.StatusBuilder target, Localization.Message.Status source);

    Localization.Message.Status.MOTD mergeMOTD2(@MappingTarget Localization.Message.Status.MOTD.MOTDBuilder target, Localization.Message.Status.MOTD motd);

    Localization.Message.Status.Players mergePlayers(@MappingTarget Localization.Message.Status.Players.PlayersBuilder target, Localization.Message.Status.Players players);

    Localization.Message.Status.Version mergeVersion(@MappingTarget Localization.Message.Status.Version.VersionBuilder target, Localization.Message.Status.Version version);

    @Mapping(target = "header", expression = "java(mergeHeader(target.build().header().toBuilder(), source.header()))")
    @Mapping(target = "footer", expression = "java(mergeFooter(target.build().footer().toBuilder(), source.footer()))")
    @Mapping(target = "playerlistname", expression = "java(mergePlayerlistname(target.build().playerlistname().toBuilder(), source.playerlistname()))")
    Localization.Message.Tab mergeTab(@MappingTarget Localization.Message.Tab.TabBuilder target, Localization.Message.Tab source);

    Localization.Message.Tab.Header mergeHeader(@MappingTarget Localization.Message.Tab.Header.HeaderBuilder target, Localization.Message.Tab.Header header);

    Localization.Message.Tab.Footer mergeFooter(@MappingTarget Localization.Message.Tab.Footer.FooterBuilder target, Localization.Message.Tab.Footer footer);

    Localization.Message.Tab.Playerlistname mergePlayerlistname(@MappingTarget Localization.Message.Tab.Playerlistname.PlayerlistnameBuilder target, Localization.Message.Tab.Playerlistname playerlistname);

    Localization.Message.Update mergeUpdate(@MappingTarget Localization.Message.Update.UpdateBuilder target, Localization.Message.Update update);

    Localization.Message.Vanilla mergeVanilla(@MappingTarget Localization.Message.Vanilla.VanillaBuilder target, Localization.Message.Vanilla vanilla);

    Localization.ListTypeMessage mergeListTypeMessage(@MappingTarget Localization.ListTypeMessage.ListTypeMessageBuilder target, Localization.ListTypeMessage listTypeMessage);

}