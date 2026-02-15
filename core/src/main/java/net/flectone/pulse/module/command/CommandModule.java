package net.flectone.pulse.module.command;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.module.AbstractModule;
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
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CommandModule extends AbstractModule {

    private final FileFacade fileFacade;

    @Override
    public ImmutableList.Builder<@NonNull Class<? extends AbstractModule>> childrenBuilder() {
        return super.childrenBuilder().add(
                AfkModule.class,
                AnonModule.class,
                BallModule.class,
                BanModule.class,
                BanlistModule.class,
                BroadcastModule.class,
                ChatcolorModule.class,
                ChatsettingModule.class,
                ClearchatModule.class,
                ClearmailModule.class,
                CoinModule.class,
                DeletemessageModule.class,
                DiceModule.class,
                DoModule.class,
                EmitModule.class,
                FlectonepulseModule.class,
                GeolocateModule.class,
                HelperModule.class,
                IgnoreModule.class,
                IgnorelistModule.class,
                KickModule.class,
                MailModule.class,
                MaintenanceModule.class,
                MeModule.class,
                MuteModule.class,
                MutelistModule.class,
                NicknameModule.class,
                OnlineModule.class,
                PingModule.class,
                PollModule.class,
                ReplyModule.class,
                RockpaperscissorsModule.class,
                SpriteModule.class,
                SpyModule.class,
                StreamModule.class,
                SymbolModule.class,
                TellModule.class,
                TictactoeModule.class,
                ToponlineModule.class,
                TranslatetoModule.class,
                TryModule.class,
                UnbanModule.class,
                UnmuteModule.class,
                UnwarnModule.class,
                WarnModule.class,
                WarnlistModule.class
        );
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder().add(permission().seeInvisiblePlayersInSuggest());
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