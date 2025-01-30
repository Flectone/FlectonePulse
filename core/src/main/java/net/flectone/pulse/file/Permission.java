package net.flectone.pulse.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.TagType;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings({"FieldMayBeFinal", "unused"})
@Comment(
        value = {
                @CommentValue("  ___       ___  __  ___  __        ___ "),
                @CommentValue(" |__  |    |__  /  `  |  /  \\ |\\ | |__"),
                @CommentValue(" |    |___ |___ \\__,  |  \\__/ | \\| |___"),
                @CommentValue("  __             __   ___ "),
                @CommentValue(" |__) |  | |    /__` |__  "),
                @CommentValue(" |    \\__/ |___ .__/ |___   /\\"),
                @CommentValue("                           /  \\"),
                @CommentValue(" __/\\___  ____/\\_____  ___/    \\______"),
                @CommentValue("        \\/           \\/  "),
                @CommentValue(" "),
        },
        at = Comment.At.PREPEND
)
@Getter
public final class Permission extends FileSerializable implements IModule {

    public Permission(Path projectPath) {
        super(projectPath.resolve("permission.yml"));
    }

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/")})
    private PermissionEntry module = new PermissionEntry("flectonepulse.module", Type.TRUE);

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/")})
    private Command command = new Command();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/integration/")})
    private Integration integration = new Integration();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/")})
    private Message message = new Message();

    @Getter
    public static final class Command implements ICommand, IPermission {

        private String name = "flectonepulse.module.command";
        private Type type = Type.TRUE;

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/afk/")})
        private Afk afk = new Afk();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/ball/")})
        private Ball ball = new Ball();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/ban/")})
        private Ban ban = new Ban();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/banlist/")})
        private Banlist banlist = new Banlist();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/broadcast/")})
        private Broadcast broadcast = new Broadcast();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/chatcolor/")})
        private Chatcolor chatcolor = new Chatcolor();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/chatsetting/")})
        private Chatsetting chatsetting = new Chatsetting();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/clearchat/")})
        private Clearchat clearchat = new Clearchat();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/clearmail/")})
        private Clearmail clearmail = new Clearmail();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/coin/")})
        private Coin coin = new Coin();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/dice/")})
        private Dice dice = new Dice();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/do/")})
        private Do Do = new Do();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/flectonepulse/")})
        private Flectonepulse flectonepulse = new Flectonepulse();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/geolocate/")})
        private Geolocate geolocate = new Geolocate();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/helper/")})
        private Helper helper = new Helper();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/ignore/")})
        private Ignore ignore = new Ignore();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/ignorelist/")})
        private Ignorelist ignorelist = new Ignorelist();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/kick/")})
        private Kick kick = new Kick();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/mail/")})
        private Mail mail = new Mail();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/maintenace/")})
        private Maintenance maintenance = new Maintenance();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/mark/")})
        private Mark mark = new Mark();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/me/")})
        private Me me = new Me();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/mute/")})
        private Mute mute = new Mute();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/mutelist/")})
        private Mutelist mutelist = new Mutelist();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/online/")})
        private Online online = new Online();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/ping/")})
        private Ping ping = new Ping();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/poll/")})
        private Poll poll = new Poll();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/reply/")})
        private Reply reply = new Reply();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/rockpaperscissors/")})
        private Rockpaperscissors rockpaperscissors = new Rockpaperscissors();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/spit/")})
        private Spit spit = new Spit();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/spy/")})
        private Spy spy = new Spy();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/stream/")})
        private Stream stream = new Stream();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/symbol/")})
        private Symbol symbol = new Symbol();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/tell/")})
        private Tell tell = new Tell();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/tictactoe/")})
        private Tictactoe tictactoe = new Tictactoe();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/translateto/")})
        private Translateto translateto = new Translateto();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/try/")})
        private Try Try = new Try();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/unban/")})
        private Unban unban = new Unban();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/unmute/")})
        private Unmute unmute = new Unmute();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/unwarn/")})
        private Unwarn unwarn = new Unwarn();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/warn/")})
        private Warn warn = new Warn();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/command/warnlist/")})
        private Warnlist warnlist = new Warnlist();

        @Getter
        public static final class Afk implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.afk";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.afk.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.afk.sound", Type.TRUE);
        }

        @Getter
        public static final class Ball implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.ball";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.ball.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.ball.sound", Type.TRUE);
        }

        @Getter
        public static final class Ban implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.ban";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.ban.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.ban.sound", Type.TRUE);
        }

        @Getter
        public static final class Banlist implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.banlist";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.banlist.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.banlist.sound", Type.TRUE);
        }

        @Getter
        public static final class Broadcast implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.broadcast";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.broadcast.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.broadcast.sound", Type.TRUE);
        }

        @Getter
        public static final class Chatcolor implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.chatcolor";
            private Type type = Type.TRUE;
            private PermissionEntry other = new PermissionEntry("flectonepulse.module.command.chatcolor.other", Type.OP);
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.chatcolor.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.chatcolor.sound", Type.TRUE);
        }

        @Getter
        public static final class Chatsetting implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.chatsetting";
            private Type type = Type.TRUE;
            private Map<FPlayer.Setting, Chatsetting.SettingItem> settings = new LinkedHashMap<>(){
                {
                    put(FPlayer.Setting.CHAT, new SettingItem("flectonepulse.module.command.chatsetting.chat", Type.TRUE));
                    put(FPlayer.Setting.COLOR, new SettingItem("flectonepulse.module.command.chatsetting.color", Type.TRUE));
                    put(FPlayer.Setting.STREAM, new SettingItem("flectonepulse.module.command.chatsetting.stream", Type.OP));
                    put(FPlayer.Setting.SPY, new SettingItem("flectonepulse.module.command.chatsetting.spy", Type.OP));
                    put(FPlayer.Setting.ADVANCEMENT, new SettingItem("flectonepulse.module.command.chatsetting.advancement", Type.TRUE));
                    put(FPlayer.Setting.DEATH, new SettingItem("flectonepulse.module.command.chatsetting.death", Type.TRUE));
                    put(FPlayer.Setting.JOIN, new SettingItem("flectonepulse.module.command.chatsetting.join", Type.TRUE));
                    put(FPlayer.Setting.QUIT, new SettingItem("flectonepulse.module.command.chatsetting.quit", Type.TRUE));
                    put(FPlayer.Setting.AUTO, new SettingItem("flectonepulse.module.command.chatsetting.auto", Type.TRUE));
                    put(FPlayer.Setting.ME, new SettingItem("flectonepulse.module.command.chatsetting.me", Type.TRUE));
                    put(FPlayer.Setting.TRY, new SettingItem("flectonepulse.module.command.chatsetting.try", Type.TRUE));
                    put(FPlayer.Setting.DICE, new SettingItem("flectonepulse.module.command.chatsetting.dice", Type.TRUE));
                    put(FPlayer.Setting.BALL, new SettingItem("flectonepulse.module.command.chatsetting.ball", Type.TRUE));
                    put(FPlayer.Setting.MUTE, new SettingItem("flectonepulse.module.command.chatsetting.mute", Type.TRUE));
                    put(FPlayer.Setting.BAN, new SettingItem("flectonepulse.module.command.chatsetting.ban", Type.TRUE));
                    put(FPlayer.Setting.WARN, new SettingItem("flectonepulse.module.command.chatsetting.warn", Type.TRUE));
                    put(FPlayer.Setting.TELL, new SettingItem("flectonepulse.module.command.chatsetting.tell", Type.TRUE));
                    put(FPlayer.Setting.REPLY, new SettingItem("flectonepulse.module.command.chatsetting.reply", Type.TRUE));
                    put(FPlayer.Setting.MAIL, new SettingItem("flectonepulse.module.command.chatsetting.mail", Type.TRUE));
                    put(FPlayer.Setting.TICTACTOE, new SettingItem("flectonepulse.module.command.chatsetting.tictactoe", Type.TRUE));
                    put(FPlayer.Setting.KICK, new SettingItem("flectonepulse.module.command.chatsetting.kick", Type.TRUE));
                    put(FPlayer.Setting.TRANSLATETO, new SettingItem("flectonepulse.module.command.chatsetting.translateto", Type.TRUE));
                    put(FPlayer.Setting.BROADCAST, new SettingItem("flectonepulse.module.command.chatsetting.broadcast", Type.TRUE));
                    put(FPlayer.Setting.DO, new SettingItem("flectonepulse.module.command.chatsetting.do", Type.TRUE));
                    put(FPlayer.Setting.COIN, new SettingItem("flectonepulse.module.command.chatsetting.coin", Type.TRUE));
                    put(FPlayer.Setting.AFK, new SettingItem("flectonepulse.module.command.chatsetting.afk", Type.TRUE));
                    put(FPlayer.Setting.POLL, new SettingItem("flectonepulse.module.command.chatsetting.poll", Type.TRUE));
                    put(FPlayer.Setting.SPIT, new SettingItem("flectonepulse.module.command.chatsetting.spit", Type.TRUE));
                    put(FPlayer.Setting.GREETING, new SettingItem("flectonepulse.module.command.chatsetting.greeting", Type.TRUE));
                    put(FPlayer.Setting.ROCKPAPERSCISSORS, new SettingItem("flectonepulse.module.command.chatsetting.rockpaperscissors", Type.TRUE));
                    put(FPlayer.Setting.DISCORD, new SettingItem("flectonepulse.module.command.chatsetting.discord", Type.TRUE));
                    put(FPlayer.Setting.TELEGRAM, new SettingItem("flectonepulse.module.command.chatsetting.telegram", Type.TRUE));
                    put(FPlayer.Setting.TWITCH, new SettingItem("flectonepulse.module.command.chatsetting.twitch", Type.TRUE));
                }
            };
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.chatsetting.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.chatsetting.sound", Type.TRUE);

            @Getter
            @NoArgsConstructor
            @AllArgsConstructor
            public static final class SettingItem implements IPermission {
                private String name = "flectonepulse.module.command.chatsetting";
                private Type type = Type.TRUE;
            }
        }

        @Getter
        public static final class Clearchat implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.clearchat";
            private Type type = Type.TRUE;
            private PermissionEntry other = new PermissionEntry("flectonepulse.module.command.clearchat.other", Type.OP);
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.clearchat.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.clearchat.sound", Type.TRUE);
        }

        @Getter
        public static final class Clearmail implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.clearmail";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.clearmail.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.clearmail.sound", Type.TRUE);
        }

        @Getter
        public static final class Coin implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.coin";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.coin.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.coin.sound", Type.TRUE);
        }

        @Getter
        public static final class Dice implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.dice";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.dice.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.dice.sound", Type.TRUE);
        }

        @Getter
        public static final class Do implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.do";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.do.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.do.sound", Type.TRUE);
        }

        @Getter
        public static final class Flectonepulse implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.flectonepulse";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.flectonepulse.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.flectonepulse.sound", Type.TRUE);
        }

        @Getter
        public static final class Geolocate implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.geolocate";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.geolocate.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.geolocate.sound", Type.TRUE);
        }

        @Getter
        public static final class Helper implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.helper";
            private Type type = Type.TRUE;
            private PermissionEntry see = new PermissionEntry("flectonepulse.module.command.helper.see", Type.OP);
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.helper.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.helper.sound", Type.TRUE);
        }

        @Getter
        public static final class Ignore implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.ignore";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.ignore.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.ignore.sound", Type.TRUE);
        }

        @Getter
        public static final class Ignorelist implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.ignorelist";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.ignorelist.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.ignorelist.sound", Type.TRUE);
        }

        @Getter
        public static final class Kick implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.kick";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.kick.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.kick.sound", Type.TRUE);
        }

        @Getter
        public static final class Mail implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.mail";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.mail.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.mail.sound", Type.TRUE);
        }

        @Getter
        public static final class Maintenance implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.maintenance";
            private Type type = Type.OP;
            private PermissionEntry join = new PermissionEntry("flectonepulse.module.command.maintenance.join", Type.OP);
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.maintenance.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.maintenance.sound", Type.TRUE);
        }

        @Getter
        public static final class Mark implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.mark";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.mark.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.mark.sound", Type.TRUE);
        }

        @Getter
        public static final class Me implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.me";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.me.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.me.sound", Type.TRUE);
        }

        @Getter
        public static final class Mute implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.mute";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.mute.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.mute.sound", Type.TRUE);
        }

        @Getter
        public static final class Mutelist implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.mutelist";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.mutelist.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.mutelist.sound", Type.TRUE);
        }

        @Getter
        public static final class Online implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.online";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.online.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.online.sound", Type.TRUE);
        }

        @Getter
        public static final class Ping implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.ping";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.ping.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.ping.sound", Type.TRUE);
        }

        @Getter
        public static final class Poll implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.poll";
            private Type type = Type.TRUE;
            private PermissionEntry create = new PermissionEntry("flectonepulse.module.command.poll.create", Type.OP);
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.poll.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.poll.sound", Type.TRUE);
        }

        @Getter
        public static final class Reply implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.reply";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.reply.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.reply.sound", Type.TRUE);
        }

        @Getter
        public static final class Rockpaperscissors implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.rockpaperscissors";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.rockpaperscissors.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.rockpaperscissors.sound", Type.TRUE);
        }

        @Getter
        public static final class Spit implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.spit";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.spit.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.spit.sound", Type.TRUE);
        }

        @Getter
        public static final class Spy implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.spy";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.spy.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.spy.sound", Type.TRUE);
        }

        @Getter
        public static final class Stream implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.stream";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.stream.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.stream.sound", Type.TRUE);
        }

        @Getter
        public static final class Symbol implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.symbol";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.symbol.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.symbol.sound", Type.TRUE);
        }

        @Getter
        public static final class Tell implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.tell";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.tell.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.tell.sound", Type.TRUE);
        }

        @Getter
        public static final class Tictactoe implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.tictactoe";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.tictactoe.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.tictactoe.sound", Type.TRUE);
        }

        @Getter
        public static final class Translateto implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.translateto";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.translateto.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.translateto.sound", Type.TRUE);
        }

        @Getter
        public static final class Try implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.try";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.try.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.try.sound", Type.TRUE);
        }

        @Getter
        public static final class Unban implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.unban";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.unban.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.unban.sound", Type.TRUE);
        }

        @Getter
        public static final class Unmute implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.unmute";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.unmute.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.unmute.sound", Type.TRUE);
        }

        @Getter
        public static final class Unwarn implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.unwarn";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.unwarn.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.unwarn.sound", Type.TRUE);
        }

        @Getter
        public static final class Warn implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.warn";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.warn.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.warn.sound", Type.TRUE);
        }

        @Getter
        public static final class Warnlist implements ISubCommand, IPermission {
            private String name = "flectonepulse.module.command.warnlist";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.warnlist.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.warnlist.sound", Type.TRUE);
        }

    }

    @Getter
    public static final class Integration implements IIntegration, IPermission {

        private String name = "flectonepulse.module.integration";
        private Type type = Type.TRUE;

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/integration/discord/")})
        private Discord discord = new Discord();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/integration/interactivechat/")})
        private Interactivechat interactivechat = new Interactivechat();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/integration/luckperms/")})
        private Luckperms luckperms = new Luckperms();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/integration/placeholderapi/")})
        private Placeholderapi placeholderapi = new Placeholderapi();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/integration/plasmovoice/")})
        private Plasmovoice plasmovoice = new Plasmovoice();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/integration/simplevoice/")})
        private Simplevoice simplevoice = new Simplevoice();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/integration/skinsrestorer/")})
        private Skinsrestorer skinsrestorer = new Skinsrestorer();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/integration/supervanish/")})
        private Supervanish supervanish = new Supervanish();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/integration/telegram/")})
        private Telegram telegram = new Telegram();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/integration/twitch/")})
        private Twitch twitch = new Twitch();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/integration/vault/")})
        private Vault vault = new Vault();

        @Getter
        public static final class Discord implements ISubIntegration, IPermission {
            private String name = "flectonepulse.module.integration.discord";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Interactivechat implements ISubIntegration, IPermission {
            private String name = "flectonepulse.module.integration.interactivechat";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Luckperms implements ISubIntegration, IPermission {
            private String name = "flectonepulse.module.integration.luckperms";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Placeholderapi implements ISubIntegration, IPermission {
            private String name = "flectonepulse.module.integration.placeholderapi";
            private Type type = Type.TRUE;
            private PermissionEntry use = new PermissionEntry("flectonepulse.module.integration.placeholderapi.use", Type.OP);
        }

        @Getter
        public static final class Plasmovoice implements ISubIntegration, IPermission {
            private String name = "flectonepulse.module.integration.plasmovoice";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Simplevoice implements ISubIntegration, IPermission {
            private String name = "flectonepulse.module.integration.simplevoice";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Skinsrestorer implements ISubIntegration, IPermission {
            private String name = "flectonepulse.module.integration.skinsrestorer";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Supervanish implements ISubIntegration, IPermission {
            private String name = "flectonepulse.module.integration.supervanish";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Telegram implements ISubIntegration, IPermission {
            private String name = "flectonepulse.module.integration.telegram";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Twitch implements ISubIntegration, IPermission {
            private String name = "flectonepulse.module.integration.twitch";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Vault implements ISubIntegration, IPermission {
            private String name = "flectonepulse.module.integration.vault";
            private Type type = Type.TRUE;
        }
    }

    @Getter
    public static final class Message implements IMessage, IPermission {

        private String name = "flectonepulse.module.message";
        private Type type = Type.TRUE;

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/advancement/")})
        private Advancement advancement = new Advancement();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/anvil/")})
        private Anvil anvil = new Anvil();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/auto/")})
        private Auto auto = new Auto();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/book/")})
        private Book book = new Book();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/brand/")})
        private Brand brand = new Brand();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/bubble/")})
        private Bubble bubble = new Bubble();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/chat/")})
        private Chat chat = new Chat();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/clear/")})
        private Clear clear = new Clear();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/contact/")})
        private Contact contact = new Contact();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/death/")})
        private Death death = new Death();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/deop/")})
        private Deop deop = new Deop();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/enchant/")})
        private Enchant enchant = new Enchant();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/format/")})
        private Format format = new Format();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/gamemode/")})
        private Gamemode gamemode = new Gamemode();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/greeting/")})
        private Greeting greeting = new Greeting();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/join/")})
        private Join join = new Join();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/objective/")})
        private Objective objective = new Objective();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/op/")})
        private Op op = new Op();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/quit/")})
        private Quit quit = new Quit();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/scoreboard/")})
        private Scoreboard scoreboard = new Scoreboard();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/seed/")})
        private Seed seed = new Seed();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/setblock/")})
        private Setblock setblock = new Setblock();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/sign/")})
        private Sign sign = new Sign();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/spawnpoint/")})
        private Spawnpoint spawnpoint = new Spawnpoint();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/status/")})
        private Status status = new Status();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/tab/")})
        private Tab tab = new Tab();

        @Getter
        public static final class Advancement implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.advancement";
            private Type type = Type.TRUE;
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.advancement.sound", Type.TRUE);
        }

        @Getter
        public static final class Anvil implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.anvil";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Auto implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.auto";
            private Type type = Type.TRUE;
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.auto.sound", Type.TRUE);
        }

        @Getter
        public static final class Book implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.book";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Brand implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.brand";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Bubble implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.bubble";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Chat implements ISubMessage, IPermission {

            private String name = "flectonepulse.module.message.chat";
            private net.flectone.pulse.file.Permission.Type type = net.flectone.pulse.file.Permission.Type.TRUE;

            private Map<String, Type> types = new LinkedHashMap<>(){
                {
                    put("local", new Type("flectonepulse.module.message.chat.local", net.flectone.pulse.file.Permission.Type.TRUE,
                            new PermissionEntry("flectonepulse.module.message.chat.local.cooldown.bypass", net.flectone.pulse.file.Permission.Type.OP),
                            new PermissionEntry("flectonepulse.module.message.chat.local.sound", net.flectone.pulse.file.Permission.Type.TRUE)
                    ));
                    put("global", new Type("flectonepulse.module.message.chat.global", net.flectone.pulse.file.Permission.Type.TRUE,
                            new PermissionEntry("flectonepulse.module.message.chat.global.cooldown.bypass", net.flectone.pulse.file.Permission.Type.OP),
                            new PermissionEntry("flectonepulse.module.message.chat.global.sound", net.flectone.pulse.file.Permission.Type.TRUE)
                    ));
                }
            };

            @Getter
            @NoArgsConstructor
            @AllArgsConstructor
            public static final class Type implements IPermission {
                private String name = "flectonepulse.module.message.chat";
                private net.flectone.pulse.file.Permission.Type type = net.flectone.pulse.file.Permission.Type.TRUE;
                private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.message.chat.cooldown.bypass", net.flectone.pulse.file.Permission.Type.OP);
                private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.chat.sound", net.flectone.pulse.file.Permission.Type.TRUE);
            }
        }

        @Getter
        public static final class Clear implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.clear";
            private Type type = Type.TRUE;
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.clear.sound", Type.TRUE);
        }

        @Getter
        public static final class Contact implements IContactMessage, IPermission {

            private String name = "flectonepulse.module.message.contact";
            private Type type = Type.TRUE;

            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/contact/afk/")})
            private Afk afk = new Afk();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/contact/knock/")})
            private Knock knock = new Knock();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/contact/mark/")})
            private Mark mark = new Mark();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/contact/rightclick/")})
            private Rightclick rightclick = new Rightclick();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/contact/sign/")})
            private Sign sign = new Sign();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/contact/spit/")})
            private Spit spit = new Spit();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/contact/unsign/")})
            private Unsign unsign = new Unsign();

            @Getter
            public static final class Afk implements ISubContactMessage, IPermission {
                private String name = "flectonepulse.module.message.contact.afk";
                private net.flectone.pulse.file.Permission.Type type = net.flectone.pulse.file.Permission.Type.TRUE;
            }

            @Getter
            public static final class Knock implements ISubContactMessage, IPermission {
                private String name = "flectonepulse.module.message.contact.knock";
                private Type type = Type.TRUE;
                private Map<String, PermissionEntry> types = new LinkedHashMap<>(){
                    {
                        put("GLASS", new PermissionEntry("flectonepulse.module.message.contact.knock.glass.sound", Type.TRUE));
                        put("DOOR", new PermissionEntry("flectonepulse.module.message.contact.knock.door.sound", Type.TRUE));
                    }
                };
                private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.message.contact.knock.cooldown.bypass", Type.OP);
            }

            @Getter
            public static final class Mark implements ISubContactMessage, IPermission {
                private String name = "flectonepulse.module.message.contact.mark";
                private Type type = Type.TRUE;
                private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.message.contact.mark.cooldown.bypass", Type.OP);
                private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.contact.mark.sound", Type.TRUE);
            }

            @Getter
            public static final class Rightclick implements ISubContactMessage, IPermission {
                private String name = "flectonepulse.module.message.contact.rightclick";
                private Type type = Type.TRUE;
                private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.message.contact.rightclick.cooldown.bypass", Type.OP);
                private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.contact.rightclick.sound", Type.TRUE);
            }

            @Getter
            public static final class Sign implements ISubContactMessage, IPermission {
                private String name = "flectonepulse.module.message.contact.sign";
                private Type type = Type.TRUE;
                private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.message.contact.sign.cooldown.bypass", Type.OP);
                private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.contact.sign.sound", Type.TRUE);
            }

            @Getter
            public static final class Spit implements ISubContactMessage, IPermission {
                private String name = "flectonepulse.module.message.contact.spit";
                private Type type = Type.TRUE;
                private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.message.contact.spit.cooldown.bypass", Type.OP);
                private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.contact.spit.sound", Type.TRUE);
            }

            @Getter
            public static final class Unsign implements ISubContactMessage, IPermission {
                private String name = "flectonepulse.module.message.contact.unsign";
                private Type type = Type.TRUE;
                private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.message.contact.unsign.cooldown.bypass", Type.OP);
                private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.contact.unsign.sound", Type.TRUE);
            }

        }

        @Getter
        public static final class Death implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.death";
            private Type type = Type.TRUE;
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.death.sound", Type.TRUE);
        }

        @Getter
        public static final class Deop implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.deop";
            private Type type = Type.TRUE;
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.deop.sound", Type.TRUE);
        }

        @Getter
        public static final class Enchant implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.enchant";
            private Type type = Type.TRUE;
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.enchant.sound", Type.TRUE);
        }

        @Getter
        public static final class Format implements IFormatMessage, IPermission {

            private String name = "flectonepulse.module.message.format";
            private Type type = Type.TRUE;

            private PermissionEntry all = new PermissionEntry("flectonepulse.module.message.format.all", Type.OP);

            private Map<TagType, PermissionEntry> tags = new LinkedHashMap<>(){
                {
                    put(TagType.PING, new PermissionEntry("flectonepulse.module.message.format.ping", Type.TRUE));
                    put(TagType.TPS, new PermissionEntry("flectonepulse.module.message.format.tps", Type.TRUE));
                    put(TagType.ONLINE, new PermissionEntry("flectonepulse.module.message.format.online", Type.TRUE));
                    put(TagType.COORDS, new PermissionEntry("flectonepulse.module.message.format.coords", Type.TRUE));
                    put(TagType.STATS, new PermissionEntry("flectonepulse.module.message.format.stats", Type.TRUE));
                    put(TagType.SKIN, new PermissionEntry("flectonepulse.module.message.format.skin", Type.TRUE));
                    put(TagType.ITEM, new PermissionEntry("flectonepulse.module.message.format.item", Type.TRUE));
                    put(TagType.URL, new PermissionEntry("flectonepulse.module.message.format.url", Type.TRUE));
                    put(TagType.IMAGE, new PermissionEntry("flectonepulse.module.message.format.image", Type.TRUE));
                    put(TagType.SPOILER, new PermissionEntry("flectonepulse.module.message.format.spoiler", Type.TRUE));
                    put(TagType.BOLD, new PermissionEntry("flectonepulse.module.message.format.bold", Type.TRUE));
                    put(TagType.ITALIC, new PermissionEntry("flectonepulse.module.message.format.italic", Type.TRUE));
                    put(TagType.UNDERLINE, new PermissionEntry("flectonepulse.module.message.format.underline", Type.TRUE));
                    put(TagType.OBFUSCATED, new PermissionEntry("flectonepulse.module.message.format.obfuscated", Type.TRUE));
                    put(TagType.STRIKETHROUGH, new PermissionEntry("flectonepulse.module.message.format.strikethrough", Type.TRUE));
                    put(TagType.HOVER, new PermissionEntry("flectonepulse.module.message.format.hover", Type.OP));
                    put(TagType.CLICK, new PermissionEntry("flectonepulse.module.message.format.click", Type.OP));
                    put(TagType.COLOR, new PermissionEntry("flectonepulse.module.message.format.color", Type.OP));
                    put(TagType.KEYBIND, new PermissionEntry("flectonepulse.module.message.format.keybind", Type.OP));
                    put(TagType.TRANSLATABLE, new PermissionEntry("flectonepulse.module.message.format.translatable", Type.OP));
                    put(TagType.TRANSLATABLE_FALLBACK, new PermissionEntry("flectonepulse.module.message.format.translatable_fallback", Type.OP));
                    put(TagType.INSERTION, new PermissionEntry("flectonepulse.module.message.format.insertion", Type.OP));
                    put(TagType.FONT, new PermissionEntry("flectonepulse.module.message.format.font", Type.OP));
                    put(TagType.DECORATION, new PermissionEntry("flectonepulse.module.message.format.decoration", Type.TRUE));
                    put(TagType.GRADIENT, new PermissionEntry("flectonepulse.module.message.format.gradient", Type.OP));
                    put(TagType.RAINBOW, new PermissionEntry("flectonepulse.module.message.format.rainbow", Type.OP));
                    put(TagType.RESET, new PermissionEntry("flectonepulse.module.message.format.reset", Type.OP));
                    put(TagType.NEWLINE, new PermissionEntry("flectonepulse.module.message.format.newline", Type.OP));
                    put(TagType.TRANSITION, new PermissionEntry("flectonepulse.module.message.format.transition", Type.OP));
                    put(TagType.SELECTOR, new PermissionEntry("flectonepulse.module.message.format.selector", Type.OP));
                    put(TagType.SCORE, new PermissionEntry("flectonepulse.module.message.format.score", Type.OP));
                    put(TagType.NBT, new PermissionEntry("flectonepulse.module.message.format.nbt", Type.OP));
                    put(TagType.PRIDE, new PermissionEntry("flectonepulse.module.message.format.pride", Type.OP));
                    put(TagType.SHADOW_COLOR, new PermissionEntry("flectonepulse.module.message.format.shadow_color", Type.OP));
                }
            };

            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/format/color/")})
            private Color color = new Color();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/format/emoji/")})
            private Emoji emoji = new Emoji();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/format/image/")})
            private Image image = new Image();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/format/mention/")})
            private Mention mention = new Mention();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/format/moderation/")})
            private Moderation moderation = new Moderation();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/format/name_/")})
            private Name name_ = new Name();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/format/questionanswer/")})
            private QuestionAnswer questionAnswer = new QuestionAnswer();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/format/spoiler/")})
            private Spoiler spoiler = new Spoiler();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/format/world/")})
            private World world = new World();

            @Getter
            public static final class Color implements ISubFormatMessage, IPermission {
                private String name = "flectonepulse.module.tag.color";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class Emoji implements ISubFormatMessage, IPermission {
                private String name = "flectonepulse.module.tag.emoji";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class Image implements ISubFormatMessage, IPermission {
                private String name = "flectonepulse.module.tag.image";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class Mention implements ISubFormatMessage, IPermission {
                private String name = "flectonepulse.module.message.format.mention";
                private Type type = Type.TRUE;
                private PermissionEntry group = new PermissionEntry("flectonepulse.module.message.format.mention.group", Type.OP);
                private PermissionEntry bypass = new PermissionEntry("flectonepulse.module.message.format.mention.bypass", Type.NOT_OP);
                private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.format.mention.sound", Type.TRUE);
            }

            @Getter
            public static final class Moderation implements IModerationFormatMessage, IPermission {

                private String name = "flectonepulse.module.message.format.moderation";
                private Type type = Type.TRUE;

                @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/format/moderation/caps/")})
                private Caps caps = new Caps();
                @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/format/moderation/swear/")})
                private Swear swear = new Swear();

                @Getter
                public static final class Caps implements ISubModerationFormatMessage, IPermission {
                    private String name = "flectonepulse.module.message.format.moderation.caps";
                    private Type type = Type.TRUE;
                    private PermissionEntry bypass = new PermissionEntry("flectonepulse.module.message.format.moderation.caps.bypass", Type.OP);
                    private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.format.moderation.caps.sound", Type.TRUE);
                }

                @Getter
                public static final class Swear implements ISubModerationFormatMessage, IPermission {
                    private String name = "flectonepulse.module.message.format.moderation.swear";
                    private Type type = Type.TRUE;
                    private PermissionEntry bypass = new PermissionEntry("flectonepulse.module.message.format.moderation.swear.bypass", Type.OP);
                    private PermissionEntry see = new PermissionEntry("flectonepulse.module.message.format.moderation.swear.see", Type.OP);
                    private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.format.moderation.swear.sound", Type.TRUE);
                }

            }

            @Getter
            public static final class Name implements ISubFormatMessage, IPermission {
                private String name = "flectonepulse.module.format.name";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class QuestionAnswer implements ISubFormatMessage, IPermission {
                private String name = "flectonepulse.module.format.questionanswer";
                private Type type = Type.TRUE;
                private Map<String, Question> questions = new LinkedHashMap<>(){
                    {
                        put("server", new Question(
                                new PermissionEntry("flectonepulse.module.format.questionanswer.server", Type.TRUE),
                                new PermissionEntry("flectonepulse.module.format.questionanswer.sound.server", Type.TRUE),
                                new PermissionEntry("flectonepulse.module.format.questionanswer.cooldown.bypass.server", Type.TRUE)
                        ));
                        put("flectone", new Question(
                                new PermissionEntry("flectonepulse.module.format.questionanswer.flectone", Type.TRUE),
                                new PermissionEntry("flectonepulse.module.format.questionanswer.sound.flectone", Type.TRUE),
                                new PermissionEntry("flectonepulse.module.format.questionanswer.cooldown.bypass.flectone", Type.TRUE)
                        ));
                    }
                };

                @Getter
                @NoArgsConstructor
                @AllArgsConstructor
                public static final class Question {
                    private PermissionEntry ask = new PermissionEntry();
                    private PermissionEntry sound = new PermissionEntry();
                    private PermissionEntry cooldownBypass = new PermissionEntry();
                }
            }

            @Getter
            public static final class Spoiler implements ISubFormatMessage, IPermission {
                private String name = "flectonepulse.module.format.spoiler";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class World implements ISubFormatMessage, IPermission {
                private String name = "flectonepulse.module.format.world";
                private Type type = Type.TRUE;
            }
        }

        @Getter
        public static final class Gamemode implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.gamemode";
            private Type type = Type.TRUE;
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.gamemode.sound", Type.TRUE);
        }

        @Getter
        public static final class Greeting implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.greeting";
            private Type type = Type.TRUE;
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.greeting.sound", Type.TRUE);
        }

        @Getter
        public static final class Join implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.join";
            private Type type = Type.TRUE;
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.join.sound", Type.TRUE);
        }

        @Getter
        public static final class Objective implements IObjectiveMessage, IPermission {

            private String name = "flectonepulse.module.message.objective";
            private Type type = Type.TRUE;

            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/objective/belowname/")})
            private Belowname belowname = new Belowname();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/objective/tabname/")})
            private Tabname tabname = new Tabname();

            @Getter
            public static final class Belowname implements ISubObjectiveMessage, IPermission {
                private String name = "flectonepulse.module.message.objective.belowname";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class Tabname implements ISubObjectiveMessage, IPermission {
                private String name = "flectonepulse.module.message.objective.tabname";
                private Type type = Type.TRUE;
            }

        }

        @Getter
        public static final class Op implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.op";
            private Type type = Type.TRUE;
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.op.sound", Type.TRUE);
        }

        @Getter
        public static final class Quit implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.quit";
            private Type type = Type.TRUE;
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.quit.sound", Type.TRUE);
        }

        @Getter
        public static final class Scoreboard implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.scoreboard";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Seed implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.seed";
            private Type type = Type.TRUE;
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.seed.sound", Type.TRUE);
        }

        @Getter
        public static final class Setblock implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.setblock";
            private Type type = Type.TRUE;
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.setblock.sound", Type.TRUE);
        }

        @Getter
        public static final class Sign implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.sign";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Spawnpoint implements ISubMessage, IPermission {
            private String name = "flectonepulse.module.message.spawnpoint";
            private Type type = Type.TRUE;
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.spawnpoint.sound", Type.TRUE);
        }

        @Getter
        public static final class Status implements IStatusMessage, IPermission {
            private String name = "flectonepulse.module.message.status";
            private Type type = Type.TRUE;

            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/status/icon/")})
            private Icon icon = new Icon();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/status/motd/")})
            private MOTD motd = new MOTD();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/status/players/")})
            private Players players = new Players();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/status/version/")})
            private Version version = new Version();

            @Getter
            public static final class MOTD implements ISubStatusMessage, IPermission {
                private String name = "flectonepulse.module.message.status.motd";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class Icon implements ISubStatusMessage, IPermission {
                private String name = "flectonepulse.module.message.status.icon";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class Players implements ISubStatusMessage, IPermission {
                private String name = "flectonepulse.module.message.status.players";
                private Type type = Type.TRUE;
                private PermissionEntry bypass = new PermissionEntry("flectonepulse.module.message.status.players.bypass", Type.OP);
            }

            @Getter
            public static final class Version implements ISubStatusMessage, IPermission {
                private String name = "flectonepulse.module.message.status.version";
                private Type type = Type.TRUE;
            }
        }

        @Getter
        public static final class Tab implements ITabMessage, IPermission {

            private String name = "flectonepulse.module.message.tab";
            private Type type = Type.TRUE;

            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/status/footer/")})
            private Footer footer = new Footer();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/status/header/")})
            private Header header = new Tab.Header();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/permission/message/status/playerlistname/")})
            private Playerlistname playerlistname = new Playerlistname();

            @Getter
            public static final class Footer implements ISubTabMessage, IPermission {
                private String name = "flectonepulse.module.message.tab.footer";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class Header implements ISubTabMessage, IPermission {
                private String name = "flectonepulse.module.message.tab.header";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class Playerlistname implements ISubTabMessage, IPermission {
                private String name = "flectonepulse.module.message.tab.footer";
                private Type type = Type.TRUE;
            }
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class PermissionEntry implements IPermission {
        private String name = "flectonepulse";
        private Type type = Type.TRUE;
    }

    public interface IPermission {
        String getName();
        Type getType();
    }

    public enum Type {
        TRUE,
        FALSE,
        OP,
        NOT_OP
    }
}
