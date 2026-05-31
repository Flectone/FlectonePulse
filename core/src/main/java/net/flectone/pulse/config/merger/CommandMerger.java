package net.flectone.pulse.config.merger;

import net.flectone.pulse.config.Command;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for merging {@link Command} configuration objects.
 * <p>
 * This interface defines mapping methods for deep merging command configurations,
 * handling nested structures through builder patterns.
 * </p>
 *
 * @author TheFaser
 * @since 1.7.1
 */
@Mapper(config = MapstructMergerConfig.class)
public interface CommandMerger {

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
    @Mapping(target = "whitelist", expression = "java(mergeWhitelist(target.build().whitelist().toBuilder(), source.whitelist()))")
    Command merge(@MappingTarget Command.CommandBuilder target, Command source);

    Command.Afk mergeAfk(@MappingTarget Command.Afk.AfkBuilder target, Command.Afk source);

    Command.Anon mergeAnon(@MappingTarget Command.Anon.AnonBuilder target, Command.Anon source);

    Command.Ball mergeBall(@MappingTarget Command.Ball.BallBuilder target, Command.Ball source);

    Command.Ban mergeBan(@MappingTarget Command.Ban.BanBuilder target, Command.Ban source);

    Command.Banlist mergeBanlist(@MappingTarget Command.Banlist.BanlistBuilder target, Command.Banlist source);

    Command.Broadcast mergeBroadcast(@MappingTarget Command.Broadcast.BroadcastBuilder target, Command.Broadcast source);

    Command.Chatcolor mergeChatcolor(@MappingTarget Command.Chatcolor.ChatcolorBuilder target, Command.Chatcolor source);

    @Mapping(target = "modern", expression = "java(mergeChatsettingModern(target.build().modern().toBuilder(), source.modern()))")
    @Mapping(target = "checkbox", expression = "java(mergeChatsettingCheckbox(target.build().checkbox().toBuilder(), source.checkbox()))")
    @Mapping(target = "menu", expression = "java(mergeChatsettingMenu(target.build().menu().toBuilder(), source.menu()))")
    Command.Chatsetting mergeChatsetting(@MappingTarget Command.Chatsetting.ChatsettingBuilder target, Command.Chatsetting source);

    Command.Chatsetting.Modern mergeChatsettingModern(@MappingTarget Command.Chatsetting.Modern.ModernBuilder target, Command.Chatsetting.Modern source);

    Command.Chatsetting.Checkbox mergeChatsettingCheckbox(@MappingTarget Command.Chatsetting.Checkbox.CheckboxBuilder target, Command.Chatsetting.Checkbox source);

    @Mapping(target = "chat", expression = "java(mergeChatsettingMenuChat(target.build().chat().toBuilder(), source.chat()))")
    @Mapping(target = "see", expression = "java(mergeChatsettingMenuColor(target.build().see().toBuilder(), source.see()))")
    @Mapping(target = "out", expression = "java(mergeChatsettingMenuColor(target.build().out().toBuilder(), source.out()))")
    Command.Chatsetting.Menu mergeChatsettingMenu(@MappingTarget Command.Chatsetting.Menu.MenuBuilder target, Command.Chatsetting.Menu source);

    Command.Chatsetting.Menu.Chat mergeChatsettingMenuChat(@MappingTarget Command.Chatsetting.Menu.Chat.ChatBuilder target, Command.Chatsetting.Menu.Chat source);

    Command.Chatsetting.Menu.Color mergeChatsettingMenuColor(@MappingTarget Command.Chatsetting.Menu.Color.ColorBuilder target, Command.Chatsetting.Menu.Color source);

    Command.Clearchat mergeClearchat(@MappingTarget Command.Clearchat.ClearchatBuilder target, Command.Clearchat source);

    Command.Clearmail mergeClearmail(@MappingTarget Command.Clearmail.ClearmailBuilder target, Command.Clearmail source);

    Command.Coin mergeCoin(@MappingTarget Command.Coin.CoinBuilder target, Command.Coin source);

    Command.Deletemessage mergeDeletemessage(@MappingTarget Command.Deletemessage.DeletemessageBuilder target, Command.Deletemessage source);

    Command.Dice mergeDice(@MappingTarget Command.Dice.DiceBuilder target, Command.Dice source);

    Command.CommandDo mergeCommandDo(@MappingTarget Command.CommandDo.CommandDoBuilder target, Command.CommandDo source);

    Command.Emit mergeEmit(@MappingTarget Command.Emit.EmitBuilder target, Command.Emit source);

    @Mapping(target = "editor", expression = "java(mergeFlectonepulseEditor(target.build().editor().toBuilder(), source.editor()))")
    Command.Flectonepulse mergeFlectonepulse(@MappingTarget Command.Flectonepulse.FlectonepulseBuilder target, Command.Flectonepulse source);

    Command.Flectonepulse.Editor mergeFlectonepulseEditor(@MappingTarget Command.Flectonepulse.Editor.EditorBuilder target, Command.Flectonepulse.Editor source);

    Command.Geolocate mergeGeolocate(@MappingTarget Command.Geolocate.GeolocateBuilder target, Command.Geolocate source);

    Command.Helper mergeHelper(@MappingTarget Command.Helper.HelperBuilder target, Command.Helper source);

    Command.Ignore mergeIgnore(@MappingTarget Command.Ignore.IgnoreBuilder target, Command.Ignore source);

    Command.Ignorelist mergeIgnorelist(@MappingTarget Command.Ignorelist.IgnorelistBuilder target, Command.Ignorelist source);

    Command.Kick mergeKick(@MappingTarget Command.Kick.KickBuilder target, Command.Kick source);

    Command.Mail mergeMail(@MappingTarget Command.Mail.MailBuilder target, Command.Mail source);

    Command.Maintenance mergeMaintenance(@MappingTarget Command.Maintenance.MaintenanceBuilder target, Command.Maintenance source);

    Command.Me mergeMe(@MappingTarget Command.Me.MeBuilder target, Command.Me source);

    Command.Mute mergeMute(@MappingTarget Command.Mute.MuteBuilder target, Command.Mute source);

    Command.Mutelist mergeMutelist(@MappingTarget Command.Mutelist.MutelistBuilder target, Command.Mutelist source);

    Command.Nickname mergeNickname(@MappingTarget Command.Nickname.NicknameBuilder target, Command.Nickname source);

    Command.Online mergeOnline(@MappingTarget Command.Online.OnlineBuilder target, Command.Online source);

    Command.Ping mergePing(@MappingTarget Command.Ping.PingBuilder target, Command.Ping source);

    Command.Poll mergePoll(@MappingTarget Command.Poll.PollBuilder target, Command.Poll source);

    Command.Reply mergeReply(@MappingTarget Command.Reply.ReplyBuilder target, Command.Reply source);

    Command.Rockpaperscissors mergeRockpaperscissors(@MappingTarget Command.Rockpaperscissors.RockpaperscissorsBuilder target, Command.Rockpaperscissors source);

    Command.Sprite mergeSprite(@MappingTarget Command.Sprite.SpriteBuilder target, Command.Sprite source);

    Command.Spy mergeSpy(@MappingTarget Command.Spy.SpyBuilder target, Command.Spy source);

    Command.Stream mergeStream(@MappingTarget Command.Stream.StreamBuilder target, Command.Stream source);

    Command.Symbol mergeSymbol(@MappingTarget Command.Symbol.SymbolBuilder target, Command.Symbol source);

    Command.Tell mergeTell(@MappingTarget Command.Tell.TellBuilder target, Command.Tell source);

    Command.Tictactoe mergeTictactoe(@MappingTarget Command.Tictactoe.TictactoeBuilder target, Command.Tictactoe source);

    Command.Toponline mergeToponline(@MappingTarget Command.Toponline.ToponlineBuilder target, Command.Toponline source);

    Command.Translateto mergeTranslateto(@MappingTarget Command.Translateto.TranslatetoBuilder target, Command.Translateto source);

    Command.CommandTry mergeCommandTry(@MappingTarget Command.CommandTry.CommandTryBuilder target, Command.CommandTry source);

    Command.Unban mergeUnban(@MappingTarget Command.Unban.UnbanBuilder target, Command.Unban source);

    Command.Unmute mergeUnmute(@MappingTarget Command.Unmute.UnmuteBuilder target, Command.Unmute source);

    Command.Unwarn mergeUnwarn(@MappingTarget Command.Unwarn.UnwarnBuilder target, Command.Unwarn source);

    Command.Warn mergeWarn(@MappingTarget Command.Warn.WarnBuilder target, Command.Warn source);

    Command.Warnlist mergeWarnlist(@MappingTarget Command.Warnlist.WarnlistBuilder target, Command.Warnlist source);

    Command.Whitelist mergeWhitelist(@MappingTarget Command.Whitelist.WhitelistBuilder target, Command.Whitelist source);

}