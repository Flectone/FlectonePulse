package net.flectone.pulse.module.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Permission;
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
import net.flectone.pulse.module.command.online.OnlineModule;
import net.flectone.pulse.module.command.ping.PingModule;
import net.flectone.pulse.module.command.poll.PollModule;
import net.flectone.pulse.module.command.reply.ReplyModule;
import net.flectone.pulse.module.command.rockpaperscissors.RockpaperscissorsModule;
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
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CommandModule extends AbstractModule {

    private final FileResolver fileResolver;

    @Override
    public void configureChildren() {
        super.configureChildren();

        addChild(AfkModule.class);
        addChild(AnonModule.class);
        addChild(BallModule.class);
        addChild(BanModule.class);
        addChild(BanlistModule.class);
        addChild(BroadcastModule.class);
        addChild(ChatcolorModule.class);
        addChild(ChatsettingModule.class);
        addChild(ClearchatModule.class);
        addChild(ClearmailModule.class);
        addChild(CoinModule.class);
        addChild(DeletemessageModule.class);
        addChild(DiceModule.class);
        addChild(DoModule.class);
        addChild(EmitModule.class);
        addChild(FlectonepulseModule.class);
        addChild(GeolocateModule.class);
        addChild(HelperModule.class);
        addChild(IgnoreModule.class);
        addChild(IgnorelistModule.class);
        addChild(KickModule.class);
        addChild(MailModule.class);
        addChild(MaintenanceModule.class);
        addChild(MeModule.class);
        addChild(MuteModule.class);
        addChild(MutelistModule.class);
        addChild(OnlineModule.class);
        addChild(PingModule.class);
        addChild(PollModule.class);
        addChild(ReplyModule.class);
        addChild(RockpaperscissorsModule.class);
        addChild(SpyModule.class);
        addChild(StreamModule.class);
        addChild(SymbolModule.class);
        addChild(TellModule.class);
        addChild(TictactoeModule.class);
        addChild(ToponlineModule.class);
        addChild(TranslatetoModule.class);
        addChild(TryModule.class);
        addChild(UnbanModule.class);
        addChild(UnmuteModule.class);
        addChild(UnwarnModule.class);
        addChild(WarnModule.class);
        addChild(WarnlistModule.class);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        registerPermission(fileResolver.getPermission().getCommand().getSeeInvisiblePlayersInSuggest());
    }

    @Override
    public Command config() {
        return fileResolver.getCommand();
    }

    @Override
    public Permission.Command permission() {
        return fileResolver.getPermission().getCommand();
    }
}