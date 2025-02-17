package net.flectone.pulse.module.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.command.afk.AfkModule;
import net.flectone.pulse.module.command.ball.BallModule;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.module.command.banlist.BanlistModule;
import net.flectone.pulse.module.command.broadcast.BroadcastModule;
import net.flectone.pulse.module.command.chatcolor.ChatcolorModule;
import net.flectone.pulse.module.command.chatsetting.ChatsettingModule;
import net.flectone.pulse.module.command.clearchat.ClearchatModule;
import net.flectone.pulse.module.command.clearmail.ClearmailModule;
import net.flectone.pulse.module.command.coin.CoinModule;
import net.flectone.pulse.module.command.dice.DiceModule;
import net.flectone.pulse.module.command.do_.DoModule;
import net.flectone.pulse.module.command.flectonepulse.FlectonepulseModule;
import net.flectone.pulse.module.command.geolocate.GeolocateModule;
import net.flectone.pulse.module.command.helper.HelperModule;
import net.flectone.pulse.module.command.ignore.IgnoreModule;
import net.flectone.pulse.module.command.ignorelist.IgnorelistModule;
import net.flectone.pulse.module.command.kick.KickModule;
import net.flectone.pulse.module.command.mail.MailModule;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.module.command.mark.MarkModule;
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
import net.flectone.pulse.module.command.translateto.TranslatetoModule;
import net.flectone.pulse.module.command.try_.TryModule;
import net.flectone.pulse.module.command.unban.UnbanModule;
import net.flectone.pulse.module.command.unmute.UnmuteModule;
import net.flectone.pulse.module.command.unwarn.UnwarnModule;
import net.flectone.pulse.module.command.warn.WarnModule;
import net.flectone.pulse.module.command.warnlist.WarnlistModule;

@Singleton
public class CommandModule extends AbstractModule {

    private final Command command;
    private final Permission.Command permission;

    @Inject
    public CommandModule(FileManager fileManager) {
        command = fileManager.getCommand();
        permission = fileManager.getPermission().getCommand();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        addChildren(AfkModule.class);
        addChildren(BallModule.class);
        addChildren(BanModule.class);
        addChildren(BanlistModule.class);
        addChildren(BroadcastModule.class);
        addChildren(ChatcolorModule.class);
        addChildren(ChatsettingModule.class);
        addChildren(ClearchatModule.class);
        addChildren(ClearmailModule.class);
        addChildren(CoinModule.class);
        addChildren(DiceModule.class);
        addChildren(DoModule.class);
        addChildren(FlectonepulseModule.class);
        addChildren(GeolocateModule.class);
        addChildren(HelperModule.class);
        addChildren(IgnoreModule.class);
        addChildren(IgnorelistModule.class);
        addChildren(KickModule.class);
        addChildren(MailModule.class);
        addChildren(MaintenanceModule.class);
        addChildren(MarkModule.class);
        addChildren(MeModule.class);
        addChildren(MuteModule.class);
        addChildren(MutelistModule.class);
        addChildren(OnlineModule.class);
        addChildren(PingModule.class);
        addChildren(PollModule.class);
        addChildren(ReplyModule.class);
        addChildren(RockpaperscissorsModule.class);
        addChildren(SpyModule.class);
        addChildren(StreamModule.class);
        addChildren(SymbolModule.class);
        addChildren(TellModule.class);
        addChildren(TictactoeModule.class);
        addChildren(TranslatetoModule.class);
        addChildren(TryModule.class);
        addChildren(UnbanModule.class);
        addChildren(UnmuteModule.class);
        addChildren(UnwarnModule.class);
        addChildren(WarnModule.class);
        addChildren(WarnlistModule.class);
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}