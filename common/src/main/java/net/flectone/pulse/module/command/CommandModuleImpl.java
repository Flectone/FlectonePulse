package net.flectone.pulse.module.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.command.afk.AfkModule;
import net.flectone.pulse.module.command.anon.AnonModule;
import net.flectone.pulse.module.command.ball.BallModule;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.module.command.banlist.BanlistModule;
import net.flectone.pulse.module.command.broadcast.BroadcastModule;
import net.flectone.pulse.module.command.chatcolor.ChatcolorModule;
import net.flectone.pulse.module.command.chatsetting.ChatsettingModule;
import net.flectone.pulse.module.command.clearchat.ClearchatModule;
import net.flectone.pulse.module.command.clearmail.ClearmailModule;
import net.flectone.pulse.module.command.coin.CoinModule;
import net.flectone.pulse.module.command.deletemessage.DeletemessageModule;
import net.flectone.pulse.module.command.dice.DiceModule;
import net.flectone.pulse.module.command.do_.DoModule;
import net.flectone.pulse.module.command.emit.EmitModule;
import net.flectone.pulse.module.command.flectonepulse.FlectonepulseModule;
import net.flectone.pulse.module.command.geolocate.GeolocateModule;
import net.flectone.pulse.module.command.helper.HelperModule;
import net.flectone.pulse.module.command.ignore.IgnoreModule;
import net.flectone.pulse.module.command.ignorelist.IgnorelistModule;
import net.flectone.pulse.module.command.kick.KickModule;
import net.flectone.pulse.module.command.mail.MailModule;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.module.command.me.MeModule;
import net.flectone.pulse.module.command.minesweeper.MinesweeperModule;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.module.command.mutelist.MutelistModule;
import net.flectone.pulse.module.command.nickname.NicknameModule;
import net.flectone.pulse.module.command.online.OnlineModule;
import net.flectone.pulse.module.command.ping.PingModule;
import net.flectone.pulse.module.command.poll.PollModule;
import net.flectone.pulse.module.command.reply.ReplyModule;
import net.flectone.pulse.module.command.rockpaperscissors.RockpaperscissorsModule;
import net.flectone.pulse.module.command.sprite.SpriteModule;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.command.stream.StreamModule;
import net.flectone.pulse.module.command.symbol.SymbolModule;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.module.command.tictactoe.TictactoeModule;
import net.flectone.pulse.module.command.toponline.ToponlineModule;
import net.flectone.pulse.module.command.translateto.TranslatetoModule;
import net.flectone.pulse.module.command.try_.TryModule;
import net.flectone.pulse.module.command.unban.UnbanModule;
import net.flectone.pulse.module.command.unmute.UnmuteModule;
import net.flectone.pulse.module.command.unwarn.UnwarnModule;
import net.flectone.pulse.module.command.warn.WarnModule;
import net.flectone.pulse.module.command.warnlist.WarnlistModule;
import net.flectone.pulse.module.command.whitelist.WhitelistModule;
import net.flectone.pulse.module.command.whois.WhoisModule;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CommandModuleImpl implements CommandModule {

    private final FileFacade fileFacade;

    @Override
    public Set<@NonNull Class<? extends ModuleSimple>> children() {
        Set<@NonNull Class<? extends ModuleSimple>> children = new LinkedHashSet<>(CommandModule.super.children());
        children.add(AfkModule.class);
        children.add(AnonModule.class);
        children.add(BallModule.class);
        children.add(BanModule.class);
        children.add(BanlistModule.class);
        children.add(BroadcastModule.class);
        children.add(ChatcolorModule.class);
        children.add(ChatsettingModule.class);
        children.add(ClearchatModule.class);
        children.add(ClearmailModule.class);
        children.add(CoinModule.class);
        children.add(DeletemessageModule.class);
        children.add(DiceModule.class);
        children.add(DoModule.class);
        children.add(EmitModule.class);
        children.add(FlectonepulseModule.class);
        children.add(GeolocateModule.class);
        children.add(HelperModule.class);
        children.add(IgnoreModule.class);
        children.add(IgnorelistModule.class);
        children.add(KickModule.class);
        children.add(MailModule.class);
        children.add(MaintenanceModule.class);
        children.add(MeModule.class);
        children.add(MinesweeperModule.class);
        children.add(MuteModule.class);
        children.add(MutelistModule.class);
        children.add(NicknameModule.class);
        children.add(OnlineModule.class);
        children.add(PingModule.class);
        children.add(PollModule.class);
        children.add(ReplyModule.class);
        children.add(RockpaperscissorsModule.class);
        children.add(SpriteModule.class);
        children.add(SpyModule.class);
        children.add(StreamModule.class);
        children.add(SymbolModule.class);
        children.add(TellModule.class);
        children.add(TictactoeModule.class);
        children.add(ToponlineModule.class);
        children.add(TranslatetoModule.class);
        children.add(TryModule.class);
        children.add(UnbanModule.class);
        children.add(UnmuteModule.class);
        children.add(UnwarnModule.class);
        children.add(WarnModule.class);
        children.add(WarnlistModule.class);
        children.add(WhitelistModule.class);
        children.add(WhoisModule.class);
        return Collections.unmodifiableSet(children);
    }

    @Override
    public Set<PermissionSetting> permissions() {
        Set<PermissionSetting> permissions = new LinkedHashSet<>(CommandModule.super.permissions());
        permissions.add(permission().seeInvisiblePlayersInSuggest());
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND;
    }

    @Override
    public Command config() {
        return fileFacade.command();
    }

    @Override
    public Permission.Command permission() {
        return fileFacade.permission().command();
    }
}