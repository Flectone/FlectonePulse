package net.flectone.pulse.config;

import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.OptBoolean;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.util.constant.AdventureTag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings({"FieldMayBeFinal", "unused"})
@Getter
public final class Permission extends YamlFile implements ModuleConfig {

    public Permission(Path projectPath) {
        super(projectPath.resolve("permission.yml"));
    }

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/message")
    private PermissionEntry module = new PermissionEntry("flectonepulse.module", Type.TRUE);

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/")
    private Command command = new Command();

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/")
    private Integration integration = new Integration();

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/")
    private Message message = new Message();

    @Getter
    public static final class Command implements CommandConfig, IPermission {

        private String name = "flectonepulse.module.command";
        private Type type = Type.TRUE;

        private PermissionEntry seeInvisiblePlayersInSuggest = new PermissionEntry("flectonepulse.module.command.see_invisible_players_in_suggest", Type.OP);

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/afk/")
        private Afk afk = new Afk();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/anon/")
        private Anon anon = new Anon();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ball/")
        private Ball ball = new Ball();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ban/")
        private Ban ban = new Ban();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/banlist/")
        private Banlist banlist = new Banlist();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/broadcast/")
        private Broadcast broadcast = new Broadcast();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/chatcolor/")
        private Chatcolor chatcolor = new Chatcolor();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/chatsetting/")
        private Chatsetting chatsetting = new Chatsetting();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/clearchat/")
        private Clearchat clearchat = new Clearchat();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/clearmail/")
        private Clearmail clearmail = new Clearmail();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/coin/")
        private Coin coin = new Coin();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/deletemessage/")
        private Deletemessage deletemessage = new Deletemessage();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/dice/")
        private Dice dice = new Dice();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/do/")
        @JsonProperty("do")
        private Do Do = new Do();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/flectonepulse/")
        private Flectonepulse flectonepulse = new Flectonepulse();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/geolocate/")
        private Geolocate geolocate = new Geolocate();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/helper/")
        private Helper helper = new Helper();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ignore/")
        private Ignore ignore = new Ignore();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ignorelist/")
        private Ignorelist ignorelist = new Ignorelist();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/kick/")
        private Kick kick = new Kick();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/mail/")
        private Mail mail = new Mail();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/maintenance/")
        private Maintenance maintenance = new Maintenance();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/me/")
        private Me me = new Me();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/mute/")
        private Mute mute = new Mute();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/mutelist/")
        private Mutelist mutelist = new Mutelist();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/online/")
        private Online online = new Online();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ping/")
        private Ping ping = new Ping();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/poll/")
        private Poll poll = new Poll();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/reply/")
        private Reply reply = new Reply();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/rockpaperscissors/")
        private Rockpaperscissors rockpaperscissors = new Rockpaperscissors();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/spy/")
        private Spy spy = new Spy();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/stream/")
        private Stream stream = new Stream();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/symbol/")
        private Symbol symbol = new Symbol();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/tell/")
        private Tell tell = new Tell();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/tictactoe/")
        private Tictactoe tictactoe = new Tictactoe();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/toponline/")
        private Toponline toponline = new Toponline();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/translateto/")
        private Translateto translateto = new Translateto();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/try/")
        @JsonProperty("try")
        private Try Try = new Try();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/unban/")
        private Unban unban = new Unban();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/unmute/")
        private Unmute unmute = new Unmute();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/unwarn/")
        private Unwarn unwarn = new Unwarn();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/warn/")
        private Warn warn = new Warn();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/warnlist/")
        private Warnlist warnlist = new Warnlist();

        @Getter
        public static final class Afk implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.afk";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.afk.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.afk.sound", Type.TRUE);
        }

        @Getter
        public static final class Anon implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.anon";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.anon.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.anon.sound", Type.TRUE);
        }

        @Getter
        public static final class Ball implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.ball";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.ball.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.ball.sound", Type.TRUE);
        }

        @Getter
        public static final class Ban implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.ban";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.ban.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.ban.sound", Type.TRUE);
        }

        @Getter
        public static final class Banlist implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.banlist";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.banlist.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.banlist.sound", Type.TRUE);
        }

        @Getter
        public static final class Broadcast implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.broadcast";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.broadcast.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.broadcast.sound", Type.TRUE);
        }

        @Getter
        public static final class Chatcolor implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.chatcolor";
            private Type type = Type.TRUE;
            private PermissionEntry other = new PermissionEntry("flectonepulse.module.command.chatcolor.other", Type.OP);
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.chatcolor.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.chatcolor.sound", Type.TRUE);
            private Map<net.flectone.pulse.model.FColor.Type, PermissionEntry> colors = new EnumMap<>(FColor.Type.class){
                {
                    put(net.flectone.pulse.model.FColor.Type.OUT, new PermissionEntry("flectonepulse.module.command.chatcolor.out", Type.OP));
                    put(net.flectone.pulse.model.FColor.Type.SEE, new PermissionEntry("flectonepulse.module.command.chatcolor.see", Type.TRUE));
                }
            };
        }

        @Getter
        public static final class Chatsetting implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.chatsetting";
            private Type type = Type.TRUE;

            private Map<String, Chatsetting.SettingItem> settings = new LinkedHashMap<>(){
                {
                    put(SettingText.CHAT_NAME.name(), new SettingItem("flectonepulse.module.command.chatsetting.chat_name", Type.TRUE));
                    put("FCOLOR_" + FColor.Type.SEE.name(), new SettingItem("flectonepulse.module.command.chatsetting.fcolor_see", Type.TRUE));
                    put("FCOLOR_" + FColor.Type.OUT.name(), new SettingItem("flectonepulse.module.command.chatsetting.fcolor_out", Type.OP));
                    put(MessageType.AFK.name(), new SettingItem("flectonepulse.module.command.chatsetting.afk", Type.TRUE));
                    put(MessageType.ADVANCEMENT.name(), new SettingItem("flectonepulse.module.command.chatsetting.advancement", Type.TRUE));
                    put(MessageType.CHAT.name(), new SettingItem("flectonepulse.module.command.chatsetting.chat", Type.TRUE));
                    put(MessageType.COMMAND_ANON.name(), new SettingItem("flectonepulse.module.command.chatsetting.command_anon", Type.TRUE));
                    put(MessageType.COMMAND_BALL.name(), new SettingItem("flectonepulse.module.command.chatsetting.command_ball", Type.TRUE));
                    put(MessageType.COMMAND_BROADCAST.name(), new SettingItem("flectonepulse.module.command.chatsetting.command_broadcast", Type.TRUE));
                    put(MessageType.COMMAND_COIN.name(), new SettingItem("flectonepulse.module.command.chatsetting.command_coin", Type.TRUE));
                    put(MessageType.COMMAND_DICE.name(), new SettingItem("flectonepulse.module.command.chatsetting.command_dice", Type.TRUE));
                    put(MessageType.COMMAND_DO.name(), new SettingItem("flectonepulse.module.command.chatsetting.command_do", Type.TRUE));
                    put(MessageType.COMMAND_MAIL.name(), new SettingItem("flectonepulse.module.command.chatsetting.command_mail", Type.TRUE));
                    put(MessageType.COMMAND_ME.name(), new SettingItem("flectonepulse.module.command.chatsetting.command_me", Type.TRUE));
                    put(MessageType.COMMAND_POLL.name(), new SettingItem("flectonepulse.module.command.chatsetting.command_poll", Type.TRUE));
                    put(MessageType.COMMAND_ROCKPAPERSCISSORS.name(), new SettingItem("flectonepulse.module.command.chatsetting.command_rockpaperscissors", Type.TRUE));
                    put(MessageType.COMMAND_STREAM.name(), new SettingItem("flectonepulse.module.command.chatsetting.command_stream", Type.TRUE));
                    put(MessageType.COMMAND_TELL.name(), new SettingItem("flectonepulse.module.command.chatsetting.command_tell", Type.TRUE));
                    put(MessageType.COMMAND_TICTACTOE.name(), new SettingItem("flectonepulse.module.command.chatsetting.command_tictactoe", Type.TRUE));
                    put(MessageType.COMMAND_TRY.name(), new SettingItem("flectonepulse.module.command.chatsetting.command_try", Type.TRUE));
                    put(MessageType.DEATH.name(), new SettingItem("flectonepulse.module.command.chatsetting.death", Type.TRUE));
                    put(MessageType.FROM_DISCORD_TO_MINECRAFT.name(), new SettingItem("flectonepulse.module.command.chatsetting.from_discord_to_minecraft", Type.TRUE));
                    put(MessageType.FROM_TELEGRAM_TO_MINECRAFT.name(), new SettingItem("flectonepulse.module.command.chatsetting.from_telegram_to_minecraft", Type.TRUE));
                    put(MessageType.FROM_TWITCH_TO_MINECRAFT.name(), new SettingItem("flectonepulse.module.command.chatsetting.from_twitch_to_minecraft", Type.TRUE));
                    put(MessageType.JOIN.name(), new SettingItem("flectonepulse.module.command.chatsetting.join", Type.TRUE));
                    put(MessageType.QUIT.name(), new SettingItem("flectonepulse.module.command.chatsetting.quit", Type.TRUE));
                    put(MessageType.SLEEP.name(), new SettingItem("flectonepulse.module.command.chatsetting.sleep", Type.TRUE));
                }
            };
            private PermissionEntry other = new PermissionEntry("flectonepulse.module.command.chatsetting.other", Type.OP);
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
        public static final class Clearchat implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.clearchat";
            private Type type = Type.TRUE;
            private PermissionEntry other = new PermissionEntry("flectonepulse.module.command.clearchat.other", Type.OP);
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.clearchat.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.clearchat.sound", Type.TRUE);
        }

        @Getter
        public static final class Clearmail implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.clearmail";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.clearmail.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.clearmail.sound", Type.TRUE);
        }

        @Getter
        public static final class Coin implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.coin";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.coin.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.coin.sound", Type.TRUE);
        }

        @Getter
        public static final class Deletemessage implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.deletemessage";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.deletemessage.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.deletemessage.sound", Type.TRUE);
        }

        @Getter
        public static final class Dice implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.dice";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.dice.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.dice.sound", Type.TRUE);
        }

        @Getter
        public static final class Do implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.do";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.do.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.do.sound", Type.TRUE);
        }

        @Getter
        public static final class Flectonepulse implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.flectonepulse";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.flectonepulse.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.flectonepulse.sound", Type.TRUE);
        }

        @Getter
        public static final class Geolocate implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.geolocate";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.geolocate.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.geolocate.sound", Type.TRUE);
        }

        @Getter
        public static final class Helper implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.helper";
            private Type type = Type.TRUE;
            private PermissionEntry see = new PermissionEntry("flectonepulse.module.command.helper.see", Type.OP);
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.helper.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.helper.sound", Type.TRUE);
        }

        @Getter
        public static final class Ignore implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.ignore";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.ignore.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.ignore.sound", Type.TRUE);
        }

        @Getter
        public static final class Ignorelist implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.ignorelist";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.ignorelist.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.ignorelist.sound", Type.TRUE);
        }

        @Getter
        public static final class Kick implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.kick";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.kick.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.kick.sound", Type.TRUE);
        }

        @Getter
        public static final class Mail implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.mail";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.mail.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.mail.sound", Type.TRUE);
        }

        @Getter
        public static final class Maintenance implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.maintenance";
            private Type type = Type.OP;
            private PermissionEntry join = new PermissionEntry("flectonepulse.module.command.maintenance.join", Type.OP);
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.maintenance.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.maintenance.sound", Type.TRUE);
        }

        @Getter
        public static final class Me implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.me";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.me.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.me.sound", Type.TRUE);
        }

        @Getter
        public static final class Mute implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.mute";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.mute.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.mute.sound", Type.TRUE);
        }

        @Getter
        public static final class Mutelist implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.mutelist";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.mutelist.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.mutelist.sound", Type.TRUE);
        }

        @Getter
        public static final class Online implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.online";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.online.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.online.sound", Type.TRUE);
        }

        @Getter
        public static final class Ping implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.ping";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.ping.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.ping.sound", Type.TRUE);
        }

        @Getter
        public static final class Poll implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.poll";
            private Type type = Type.TRUE;
            private PermissionEntry create = new PermissionEntry("flectonepulse.module.command.poll.create", Type.OP);
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.poll.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.poll.sound", Type.TRUE);
        }

        @Getter
        public static final class Reply implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.reply";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.reply.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.reply.sound", Type.TRUE);
        }

        @Getter
        public static final class Rockpaperscissors implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.rockpaperscissors";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.rockpaperscissors.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.rockpaperscissors.sound", Type.TRUE);
        }

        @Getter
        public static final class Spy implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.spy";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.spy.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.spy.sound", Type.TRUE);
        }

        @Getter
        public static final class Stream implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.stream";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.stream.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.stream.sound", Type.TRUE);
        }

        @Getter
        public static final class Symbol implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.symbol";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.symbol.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.symbol.sound", Type.TRUE);
        }

        @Getter
        public static final class Tell implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.tell";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.tell.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.tell.sound", Type.TRUE);
        }

        @Getter
        public static final class Tictactoe implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.tictactoe";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.tictactoe.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.tictactoe.sound", Type.TRUE);
        }

        @Getter
        public static final class Toponline implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.toponline";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.toponline.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.toponline.sound", Type.TRUE);
        }

        @Getter
        public static final class Translateto implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.translateto";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.translateto.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.translateto.sound", Type.TRUE);
        }

        @Getter
        public static final class Try implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.try";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.try.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.try.sound", Type.TRUE);
        }

        @Getter
        public static final class Unban implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.unban";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.unban.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.unban.sound", Type.TRUE);
        }

        @Getter
        public static final class Unmute implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.unmute";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.unmute.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.unmute.sound", Type.TRUE);
        }

        @Getter
        public static final class Unwarn implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.unwarn";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.unwarn.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.unwarn.sound", Type.TRUE);
        }

        @Getter
        public static final class Warn implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.warn";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.warn.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.warn.sound", Type.TRUE);
        }

        @Getter
        public static final class Warnlist implements SubCommandConfig, ICommandPermission {
            private String name = "flectonepulse.module.command.warnlist";
            private Type type = Type.OP;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.command.warnlist.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.command.warnlist.sound", Type.TRUE);
        }

    }

    @Getter
    public static final class Integration implements IntegrationConfig, IPermission {

        private String name = "flectonepulse.module.integration";
        private Type type = Type.TRUE;

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/advancedban/")
        private Advancedban advancedban = new Advancedban();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/libertybans/")
        private Libertybans libertybans = new Libertybans();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/deepl/")
        private Deepl deepl = new Deepl();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/discord/")
        private Discord discord = new Discord();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/geyser/")
        private Geyser geyser = new Geyser();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/interactivechat/")
        private Interactivechat interactivechat = new Interactivechat();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/itemsadder/")
        private Itemsadder itemsadder = new Itemsadder();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/litebans/")
        private Litebans litebans = new Litebans();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/luckperms/")
        private Luckperms luckperms = new Luckperms();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/maintenance/")
        private Maintenance maintenance = new Maintenance();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/minimotd/")
        private MiniMOTD minimotd = new MiniMOTD();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/miniplaceholders/")
        private MiniPlaceholders miniplaceholders = new MiniPlaceholders();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/motd/")
        private MOTD motd = new MOTD();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/placeholderapi/")
        private Placeholderapi placeholderapi = new Placeholderapi();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/plasmovoice/")
        private Plasmovoice plasmovoice = new Plasmovoice();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/simplevoice/")
        private Simplevoice simplevoice = new Simplevoice();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/skinsrestorer/")
        private Skinsrestorer skinsrestorer = new Skinsrestorer();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/supervanish/")
        private Supervanish supervanish = new Supervanish();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/tab/")
        @JsonProperty("tab")
        private TAB TAB = new TAB();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/telegram/")
        private Telegram telegram = new Telegram();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/triton/")
        private Triton triton = new Triton();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/twitch/")
        private Twitch twitch = new Twitch();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/vault/")
        private Vault vault = new Vault();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/yandex/")
        private Yandex yandex = new Yandex();

        @Getter
        public static final class Advancedban implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.advancedban";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Libertybans implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.libertybans";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Deepl implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.deepl";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Discord implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.discord";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Geyser implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.geyser";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Interactivechat implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.interactivechat";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Itemsadder implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.itemsadder";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Litebans implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.litebans";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Luckperms implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.luckperms";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Maintenance implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.maintenance";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class MiniMOTD implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.minimotd";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class MiniPlaceholders implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.miniplaceholders";
            private Type type = Type.TRUE;
            private PermissionEntry use = new PermissionEntry("flectonepulse.module.integration.miniplaceholders.use", Type.OP);
        }

        @Getter
        public static final class MOTD implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.motd";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Placeholderapi implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.placeholderapi";
            private Type type = Type.TRUE;
            private PermissionEntry use = new PermissionEntry("flectonepulse.module.integration.placeholderapi.use", Type.OP);
        }

        @Getter
        public static final class Plasmovoice implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.plasmovoice";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Simplevoice implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.simplevoice";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Skinsrestorer implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.skinsrestorer";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Supervanish implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.supervanish";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class TAB implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.tab";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Telegram implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.telegram";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Triton implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.triton";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Twitch implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.twitch";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Vault implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.vault";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Yandex implements SubIntegrationConfig, IPermission {
            private String name = "flectonepulse.module.integration.yandex";
            private Type type = Type.TRUE;
        }
    }

    @Getter
    public static final class Message implements MessageConfig, IPermission {

        private String name = "flectonepulse.module.message";
        private Type type = Type.TRUE;

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/afk/")
        private Afk afk = new Afk();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/anvil/")
        private Anvil anvil = new Anvil();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/auto/")
        private Auto auto = new Auto();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/book/")
        private Book book = new Book();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/brand/")
        private Brand brand = new Brand();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/bubble/")
        private Bubble bubble = new Bubble();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/chat/")
        private Chat chat = new Chat();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/")
        private Format format = new Format();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/greeting/")
        private Greeting greeting = new Greeting();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/join/")
        private Join join = new Join();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/objective/")
        private Objective objective = new Objective();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/quit/")
        private Quit quit = new Quit();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/rightclick/")
        private Rightclick rightclick = new Rightclick();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/sidebar/")
        private Sidebar sidebar = new Sidebar();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/sign/")
        private Sign sign = new Sign();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/")
        private Status status = new Status();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/")
        private Tab tab = new Tab();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/update/")
        private Update update = new Update();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/vanilla/")
        private Vanilla vanilla = new Vanilla();

        @Getter
        public static final class Afk implements SubMessageConfig, IPermission {
            private String name = "flectonepulse.module.message.afk";
            private Permission.Type type = Permission.Type.TRUE;
        }

        @Getter
        public static final class Anvil implements SubMessageConfig, IPermission {
            private String name = "flectonepulse.module.message.anvil";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Auto implements SubMessageConfig, IPermission {
            private String name = "flectonepulse.module.message.auto";
            private Type type = Type.TRUE;

            @JsonMerge(OptBoolean.FALSE)
            private Map<String, PermissionEntry> types = new LinkedHashMap<>(){
                {
                    put("announcement", new PermissionEntry("flectonepulse.module.message.auto.sound", Type.TRUE));
                }
            };
        }

        @Getter
        public static final class Book implements SubMessageConfig, IPermission {
            private String name = "flectonepulse.module.message.book";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Brand implements SubMessageConfig, IPermission {
            private String name = "flectonepulse.module.message.brand";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Bubble implements SubMessageConfig, IPermission {
            private String name = "flectonepulse.module.message.bubble";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Chat implements SubMessageConfig, IPermission {

            private String name = "flectonepulse.module.message.chat";
            private Permission.Type type = Permission.Type.TRUE;

            @JsonMerge(OptBoolean.FALSE)
            private Map<String, Type> types = new LinkedHashMap<>(){
                {
                    put("local", new Type("flectonepulse.module.message.chat.local", Permission.Type.TRUE,
                            new PermissionEntry("flectonepulse.module.message.chat.local.cooldown.bypass", Permission.Type.OP),
                            new PermissionEntry("flectonepulse.module.message.chat.local.sound", Permission.Type.TRUE)
                    ));
                    put("global", new Type("flectonepulse.module.message.chat.global", Permission.Type.TRUE,
                            new PermissionEntry("flectonepulse.module.message.chat.global.cooldown.bypass", Permission.Type.OP),
                            new PermissionEntry("flectonepulse.module.message.chat.global.sound", Permission.Type.TRUE)
                    ));
                }
            };

            @Getter
            @NoArgsConstructor
            @AllArgsConstructor
            public static final class Type implements IPermission {
                private String name = "flectonepulse.module.message.chat";
                private Permission.Type type = Permission.Type.TRUE;
                private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.message.chat.cooldown.bypass", Permission.Type.OP);
                private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.chat.sound", Permission.Type.TRUE);
            }
        }

        @Getter
        public static final class Format implements FormatMessageConfig, IPermission {

            private String name = "flectonepulse.module.message.format";
            private Type type = Type.TRUE;

            private PermissionEntry legacyColors = new PermissionEntry("flectonepulse.module.message.format.legacy_color", Type.OP);

            @JsonMerge(OptBoolean.FALSE)
            private Map<AdventureTag, PermissionEntry> adventureTags = new LinkedHashMap<>(){
                {
                    put(AdventureTag.HOVER, new PermissionEntry("flectonepulse.module.message.format.hover", Type.OP));
                    put(AdventureTag.CLICK, new PermissionEntry("flectonepulse.module.message.format.click", Type.OP));
                    put(AdventureTag.COLOR, new PermissionEntry("flectonepulse.module.message.format.color", Type.OP));
                    put(AdventureTag.KEYBIND, new PermissionEntry("flectonepulse.module.message.format.keybind", Type.OP));
                    put(AdventureTag.TRANSLATABLE, new PermissionEntry("flectonepulse.module.message.format.translatable", Type.OP));
                    put(AdventureTag.TRANSLATABLE_FALLBACK, new PermissionEntry("flectonepulse.module.message.format.translatable_fallback", Type.OP));
                    put(AdventureTag.INSERTION, new PermissionEntry("flectonepulse.module.message.format.insertion", Type.OP));
                    put(AdventureTag.FONT, new PermissionEntry("flectonepulse.module.message.format.font", Type.OP));
                    put(AdventureTag.DECORATION, new PermissionEntry("flectonepulse.module.message.format.decoration", Type.TRUE));
                    put(AdventureTag.GRADIENT, new PermissionEntry("flectonepulse.module.message.format.gradient", Type.OP));
                    put(AdventureTag.RAINBOW, new PermissionEntry("flectonepulse.module.message.format.rainbow", Type.OP));
                    put(AdventureTag.RESET, new PermissionEntry("flectonepulse.module.message.format.reset", Type.OP));
                    put(AdventureTag.NEWLINE, new PermissionEntry("flectonepulse.module.message.format.newline", Type.OP));
                    put(AdventureTag.TRANSITION, new PermissionEntry("flectonepulse.module.message.format.transition", Type.OP));
                    put(AdventureTag.SELECTOR, new PermissionEntry("flectonepulse.module.message.format.selector", Type.OP));
                    put(AdventureTag.SCORE, new PermissionEntry("flectonepulse.module.message.format.score", Type.OP));
                    put(AdventureTag.NBT, new PermissionEntry("flectonepulse.module.message.format.nbt", Type.OP));
                    put(AdventureTag.PRIDE, new PermissionEntry("flectonepulse.module.message.format.pride", Type.OP));
                    put(AdventureTag.SHADOW_COLOR, new PermissionEntry("flectonepulse.module.message.format.shadow_color", Type.OP));
                }
            };

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/fcolor/")
            private FColor fcolor = new FColor();
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/fixation/")
            private Fixation fixation = new Fixation();
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/image/")
            private Image image = new Image();
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/mention/")
            private Mention mention = new Mention();
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/")
            private Moderation moderation = new Moderation();
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/names/")
            private Names names = new Names();
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/object/")
            private Object object = new Object();
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/questionanswer/")
            private QuestionAnswer questionAnswer = new QuestionAnswer();
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/replacement/")
            private Replacement replacement = new Replacement();
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/scoreboard/")
            private Scoreboard scoreboard = new Scoreboard();
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/spoiler/")
            private Spoiler spoiler = new Spoiler();
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/translate/")
            private Translate translate = new Translate();
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/world/")
            private World world = new World();

            @Getter
            public static final class FColor implements SubFormatMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.format.fcolor";
                private Type type = Type.TRUE;

                @JsonMerge(OptBoolean.FALSE)
                private Map<net.flectone.pulse.model.FColor.Type, PermissionEntry> colors = new LinkedHashMap<>(){
                    {
                        put(net.flectone.pulse.model.FColor.Type.OUT, new PermissionEntry("flectonepulse.module.message.format.fcolor.out", Type.TRUE));
                        put(net.flectone.pulse.model.FColor.Type.SEE, new PermissionEntry("flectonepulse.module.message.format.fcolor.see", Type.TRUE));
                    }
                };
            }

            @Getter
            public static final class Fixation implements SubFormatMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.format.fixation";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class Image implements SubFormatMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.format.image";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class Mention implements SubFormatMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.format.mention";
                private Type type = Type.TRUE;
                private PermissionEntry group = new PermissionEntry("flectonepulse.module.message.format.mention.group", Type.OP);
                private PermissionEntry bypass = new PermissionEntry("flectonepulse.module.message.format.mention.bypass", Type.FALSE);
                private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.format.mention.sound", Type.TRUE);
            }

            @Getter
            public static final class Moderation implements ModerationFormatMessageConfig, IPermission {

                private String name = "flectonepulse.module.message.format.moderation";
                private Type type = Type.TRUE;

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/caps/")
                private Caps caps = new Caps();
                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/delete/")
                private Delete delete = new Delete();
                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/newbie/")
                private Newbie newbie = new Newbie();
                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/flood/")
                private Flood flood = new Flood();
                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/swear/")
                private Swear swear = new Swear();

                @Getter
                public static final class Caps implements SubModerationFormatMessageConfig, IPermission {
                    private String name = "flectonepulse.module.message.format.moderation.caps";
                    private Type type = Type.TRUE;
                    private PermissionEntry bypass = new PermissionEntry("flectonepulse.module.message.format.moderation.caps.bypass", Type.OP);
                }

                @Getter
                public static final class Delete implements SubModerationFormatMessageConfig, IPermission {
                    private String name = "flectonepulse.module.message.format.moderation.delete";
                    private Type type = Type.OP;
                }

                @Getter
                public static final class Newbie implements SubModerationFormatMessageConfig, IPermission {
                    private String name = "flectonepulse.module.message.format.moderation.newbie";
                    private Type type = Type.TRUE;
                    private PermissionEntry bypass = new PermissionEntry("flectonepulse.module.message.format.moderation.newbie.bypass", Type.OP);
                }

                @Getter
                public static final class Flood implements SubModerationFormatMessageConfig, IPermission {
                    private String name = "flectonepulse.module.message.format.moderation.flood";
                    private Type type = Type.TRUE;
                    private PermissionEntry bypass = new PermissionEntry("flectonepulse.module.message.format.moderation.flood.bypass", Type.OP);
                }

                @Getter
                public static final class Swear implements SubModerationFormatMessageConfig, IPermission {
                    private String name = "flectonepulse.module.message.format.moderation.swear";
                    private Type type = Type.TRUE;
                    private PermissionEntry bypass = new PermissionEntry("flectonepulse.module.message.format.moderation.swear.bypass", Type.OP);
                    private PermissionEntry see = new PermissionEntry("flectonepulse.module.message.format.moderation.swear.see", Type.OP);
                }

            }

            @Getter
            public static final class Names implements SubFormatMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.format.names";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class Object implements SubFormatMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.format.object";
                private Type type = Type.TRUE;

                private PermissionEntry playerHead = new PermissionEntry("flectonepulse.module.message.format.object.player_head", Type.TRUE);
                private PermissionEntry sprite = new PermissionEntry("flectonepulse.module.message.format.object.sprite", Type.TRUE);
            }

            @Getter
            public static final class Scoreboard implements SubFormatMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.format.scoreboard";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class QuestionAnswer implements SubFormatMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.format.questionanswer";
                private Type type = Type.TRUE;

                @JsonMerge(OptBoolean.FALSE)
                private Map<String, Question> questions = new LinkedHashMap<>(){
                    {
                        put("server", new Question(
                                new PermissionEntry("flectonepulse.module.message.format.questionanswer.server", Type.TRUE),
                                new PermissionEntry("flectonepulse.module.message.format.questionanswer.sound.server", Type.TRUE),
                                new PermissionEntry("flectonepulse.module.message.format.questionanswer.cooldown.bypass.server", Type.TRUE)
                        ));
                        put("flectone", new Question(
                                new PermissionEntry("flectonepulse.module.message.format.questionanswer.flectone", Type.TRUE),
                                new PermissionEntry("flectonepulse.module.message.format.questionanswer.sound.flectone", Type.TRUE),
                                new PermissionEntry("flectonepulse.module.message.format.questionanswer.cooldown.bypass.flectone", Type.TRUE)
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
            public static final class Replacement implements SubFormatMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.format.replacement";
                private Type type = Type.TRUE;

                @JsonMerge(OptBoolean.FALSE)
                private Map<String, PermissionEntry> values = new LinkedHashMap<>() {
                    {
                        // emoticons
                        put("smile", new PermissionEntry("flectonepulse.module.message.format.replacement.smile", Type.TRUE));
                        put("big_smile", new PermissionEntry("flectonepulse.module.message.format.replacement.big_smile", Type.TRUE));
                        put("sad", new PermissionEntry("flectonepulse.module.message.format.replacement.sad", Type.TRUE));
                        put("ok_hand", new PermissionEntry("flectonepulse.module.message.format.replacement.ok_hand", Type.TRUE));
                        put("thumbs_up", new PermissionEntry("flectonepulse.module.message.format.replacement.thumbs_up", Type.TRUE));
                        put("thumbs_down", new PermissionEntry("flectonepulse.module.message.format.replacement.thumbs_down", Type.TRUE));
                        put("cool_smile", new PermissionEntry("flectonepulse.module.message.format.replacement.cool_smile", Type.TRUE));
                        put("cool_glasses", new PermissionEntry("flectonepulse.module.message.format.replacement.cool_glasses", Type.TRUE));
                        put("clown", new PermissionEntry("flectonepulse.module.message.format.replacement.clown", Type.TRUE));
                        put("heart", new PermissionEntry("flectonepulse.module.message.format.replacement.heart", Type.TRUE));
                        put("laughing", new PermissionEntry("flectonepulse.module.message.format.replacement.laughing", Type.TRUE));
                        put("confused", new PermissionEntry("flectonepulse.module.message.format.replacement.confused", Type.TRUE));
                        put("happy", new PermissionEntry("flectonepulse.module.message.format.replacement.happy", Type.TRUE));
                        put("angry", new PermissionEntry("flectonepulse.module.message.format.replacement.angry", Type.TRUE));

                        // ascii art
                        put("ascii_idk", new PermissionEntry("flectonepulse.module.message.format.replacement.ascii_idk", Type.TRUE));
                        put("ascii_angry", new PermissionEntry("flectonepulse.module.message.format.replacement.ascii_angry", Type.TRUE));
                        put("ascii_happy", new PermissionEntry("flectonepulse.module.message.format.replacement.ascii_happy", Type.TRUE));

                        // dynamic arguments
                        put("ping", new PermissionEntry("flectonepulse.module.message.format.replacement.ping", Type.TRUE));
                        put("tps", new PermissionEntry("flectonepulse.module.message.format.replacement.tps", Type.TRUE));
                        put("online", new PermissionEntry("flectonepulse.module.message.format.replacement.online", Type.TRUE));
                        put("coords", new PermissionEntry("flectonepulse.module.message.format.replacement.coords", Type.TRUE));
                        put("stats", new PermissionEntry("flectonepulse.module.message.format.replacement.stats", Type.TRUE));
                        put("skin", new PermissionEntry("flectonepulse.module.message.format.replacement.skin", Type.TRUE));
                        put("item", new PermissionEntry("flectonepulse.module.message.format.replacement.item", Type.TRUE));

                        // text formatting
                        put("url", new PermissionEntry("flectonepulse.module.message.format.replacement.url", Type.TRUE));
                        put("image", new PermissionEntry("flectonepulse.module.message.format.replacement.image", Type.TRUE));
                        put("spoiler", new PermissionEntry("flectonepulse.module.message.format.replacement.spoiler", Type.TRUE));
                        put("bold", new PermissionEntry("flectonepulse.module.message.format.replacement.bold", Type.TRUE));
                        put("italic", new PermissionEntry("flectonepulse.module.message.format.replacement.italic", Type.TRUE));
                        put("underline", new PermissionEntry("flectonepulse.module.message.format.replacement.underline", Type.TRUE));
                        put("obfuscated", new PermissionEntry("flectonepulse.module.message.format.replacement.obfuscated", Type.TRUE));
                        put("strikethrough", new PermissionEntry("flectonepulse.module.message.format.replacement.strikethrough", Type.TRUE));
                    }
                };
            }

            @Getter
            public static final class Spoiler implements SubFormatMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.format.spoiler";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class Style implements SubFormatMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.format.style";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class Translate implements SubFormatMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.format.translate";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class World implements SubFormatMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.format.world";
                private Type type = Type.TRUE;
            }
        }

        @Getter
        public static final class Greeting implements SubMessageConfig, IPermission {
            private String name = "flectonepulse.module.message.greeting";
            private Type type = Type.TRUE;
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.greeting.sound", Type.TRUE);
        }

        @Getter
        public static final class Join implements SubMessageConfig, IPermission {
            private String name = "flectonepulse.module.message.join";
            private Type type = Type.TRUE;
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.join.sound", Type.TRUE);
        }

        @Getter
        public static final class Objective implements ObjectiveMessageConfig, IPermission {

            private String name = "flectonepulse.module.message.objective";
            private Type type = Type.TRUE;

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/objective/belowname/")
            private Belowname belowname = new Belowname();
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/objective/tabname/")
            private Tabname tabname = new Tabname();

            @Getter
            public static final class Belowname implements SubObjectiveMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.objective.belowname";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class Tabname implements SubObjectiveMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.objective.tabname";
                private Type type = Type.TRUE;
            }

        }

        @Getter
        public static final class Quit implements SubMessageConfig, IPermission {
            private String name = "flectonepulse.module.message.quit";
            private Type type = Type.TRUE;
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.quit.sound", Type.TRUE);
        }

        @Getter
        public static final class Rightclick implements SubMessageConfig, IPermission {
            private String name = "flectonepulse.module.message.rightclick";
            private Type type = Type.TRUE;
            private PermissionEntry cooldownBypass = new PermissionEntry("flectonepulse.module.message.rightclick.cooldown.bypass", Type.OP);
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.rightclick.sound", Type.TRUE);
        }

        @Getter
        public static final class Sidebar implements SubMessageConfig, IPermission {
            private String name = "flectonepulse.module.message.sidebar";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Sign implements SubMessageConfig, IPermission {
            private String name = "flectonepulse.module.message.sign";
            private Type type = Type.TRUE;
        }

        @Getter
        public static final class Status implements StatusMessageConfig, IPermission {
            private String name = "flectonepulse.module.message.status";
            private Type type = Type.TRUE;

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/icon/")
            private Icon icon = new Icon();
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/motd/")
            private MOTD motd = new MOTD();
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/players/")
            private Players players = new Players();
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/version/")
            private Version version = new Version();

            @Getter
            public static final class MOTD implements SubStatusMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.status.motd";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class Icon implements SubStatusMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.status.icon";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class Players implements SubStatusMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.status.players";
                private Type type = Type.TRUE;
                private PermissionEntry bypass = new PermissionEntry("flectonepulse.module.message.status.players.bypass", Type.OP);
            }

            @Getter
            public static final class Version implements SubStatusMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.status.version";
                private Type type = Type.TRUE;
            }
        }

        @Getter
        public static final class Tab implements TabMessageConfig, IPermission {

            private String name = "flectonepulse.module.message.tab";
            private Type type = Type.TRUE;

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/footer/")
            private Footer footer = new Footer();
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/header/")
            private Header header = new Tab.Header();
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/playerlistname/")
            private Playerlistname playerlistname = new Playerlistname();

            @Getter
            public static final class Footer implements SubTabMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.tab.footer";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class Header implements SubTabMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.tab.header";
                private Type type = Type.TRUE;
            }

            @Getter
            public static final class Playerlistname implements SubTabMessageConfig, IPermission {
                private String name = "flectonepulse.module.message.tab.footer";
                private Type type = Type.TRUE;
            }
        }

        @Setter
        @Getter
        public static final class Update implements SubMessageConfig, IPermission {
            private String name = "flectonepulse.module.message.update";
            private Type type = Type.OP;
            private PermissionEntry sound = new PermissionEntry("flectonepulse.module.message.update.sound", Type.TRUE);
        }

        @Getter
        public static final class Vanilla implements SubMessageConfig, IPermission {
            private String name = "flectonepulse.module.message.vanilla";
            private Type type = Type.TRUE;
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

    public interface ICommandPermission extends IPermission {

        PermissionEntry getCooldownBypass();

        PermissionEntry getSound();

    }

    public enum Type {
        TRUE,
        FALSE,
        OP,
        NOT_OP
    }
}
