package net.flectone.pulse.file;

import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.flectone.pulse.model.Cooldown;
import net.flectone.pulse.model.Destination;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Sound;
import net.flectone.pulse.util.Range;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
public final class Command extends FileSerializable implements IModule.ICommand {

    public Command(Path projectPath) {
        super(projectPath.resolve("command.yml"));
    }

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/")})
    private boolean enable = true;

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/afk/")})
    private Afk afk = new Afk();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/ball/")})
    private Ball ball = new Ball();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/ban/")})
    private Ban ban = new Ban();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/banlist/")})
    private Banlist banlist = new Banlist();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/broadcast/")})
    private Broadcast broadcast = new Broadcast();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/chatcolor/")})
    private Chatcolor chatcolor = new Chatcolor();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/chatsetting/")})
    private Chatsetting chatsetting = new Chatsetting();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/clearchat/")})
    private Clearchat clearchat = new Clearchat();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/clearmail/")})
    private Clearmail clearmail = new Clearmail();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/coin/")})
    private Coin coin = new Coin();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/dice/")})
    private Dice dice = new Dice();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/do/")})
    private Do Do = new Do();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/flectonepulse/")})
    private Flectonepulse flectonepulse = new Flectonepulse();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/geolocate/")})
    private Geolocate geolocate = new Geolocate();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/helper/")})
    private Helper helper = new Helper();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/ignore/")})
    private Ignore ignore = new Ignore();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/ignorelist/")})
    private Ignorelist ignorelist = new Ignorelist();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/kick/")})
    private Kick kick = new Kick();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/mail/")})
    private Mail mail = new Mail();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/maintenace/")})
    private Maintenance maintenance = new Maintenance();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/mark/")})
    private Mark mark = new Mark();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/me/")})
    private Me me = new Me();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/mute/")})
    private Mute mute = new Mute();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/mutelist/")})
    private Mutelist mutelist = new Mutelist();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/online/")})
    private Online online = new Online();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/ping/")})
    private Ping ping = new Ping();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/poll/")})
    private Poll poll = new Poll();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/reply/")})
    private Reply reply = new Reply();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/rockpaperscissors/")})
    private Rockpaperscissors rockpaperscissors = new Rockpaperscissors();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/spit/")})
    private Spit spit = new Spit();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/spy/")})
    private Spy spy = new Spy();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/stream/")})
    private Stream stream = new Stream();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/symbol/")})
    private Symbol symbol = new Symbol();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/tell/")})
    private Tell tell = new Tell();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/tictactoe/")})
    private Tictactoe tictactoe = new Tictactoe();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/translateto/")})
    private Translateto translateto = new Translateto();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/try/")})
    private Try Try = new Try();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/unban/")})
    private Unban unban = new Unban();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/unmute/")})
    private Unmute unmute = new Unmute();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/unwarn/")})
    private Unwarn unwarn = new Unwarn();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/warn/")})
    private Warn warn = new Warn();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/warnlist/")})
    private Warnlist warnlist = new Warnlist();

    @Getter
    public static final class Afk implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private List<String> aliases = new ArrayList<>(List.of("afk"));
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Ball implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int range = Range.PROXY;
        private List<String> aliases = new ArrayList<>(List.of("ball"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Ban implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private boolean suggestOfflinePlayers = true;
        private boolean showConnectionAttempts = true;
        private int range = Range.PROXY;
        private List<String> aliases = new ArrayList<>(List.of("ban", "tempban"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Banlist implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int perPage = 4;
        private List<String> aliases = new ArrayList<>(List.of("banlist"));
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Broadcast implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int range = Range.PROXY;
        private List<String> aliases = new ArrayList<>(List.of("broadcast"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Chatcolor implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private List<String> aliases = new ArrayList<>(List.of("chatcolor"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Chatsetting implements ISubCommand, ICommandFile {

        private boolean enable = true;
        private List<String> aliases = new ArrayList<>(List.of("chatsetting"));
        private Map<FPlayer.Setting, SettingItem> settings = new LinkedHashMap<>(){
            {
                put(FPlayer.Setting.CHAT, new SettingItem(0, List.of("YELLOW_CONCRETE")));
                put(FPlayer.Setting.COLOR, new SettingItem(1, List.of("YELLOW_CONCRETE")));
                put(FPlayer.Setting.STREAM, new SettingItem(2, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.SPY, new SettingItem(3, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.ADVANCEMENT, new SettingItem(4, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.DEATH, new SettingItem(5, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.JOIN, new SettingItem(6, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.QUIT, new SettingItem(7, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.AUTO, new SettingItem(8, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.ME, new SettingItem(9, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.TRY, new SettingItem(10, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.DICE, new SettingItem(11, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.BALL, new SettingItem(12, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.MUTE, new SettingItem(13, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.BAN, new SettingItem(14, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.WARN, new SettingItem(15, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.TELL, new SettingItem(16, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.REPLY, new SettingItem(17, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.MAIL, new SettingItem(18, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.TICTACTOE, new SettingItem(19, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.KICK, new SettingItem(20, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.TRANSLATETO, new SettingItem(21, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.BROADCAST, new SettingItem(22, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.DO, new SettingItem(23, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.COIN, new SettingItem(24, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.AFK, new SettingItem(25, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.POLL, new SettingItem(26, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.SPIT, new SettingItem(27, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.GREETING, new SettingItem(28, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.ROCKPAPERSCISSORS, new SettingItem(29, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.DISCORD, new SettingItem(30, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.TELEGRAM, new SettingItem(31, List.of("LIME_CONCRETE", "RED_CONCRETE")));
                put(FPlayer.Setting.TWITCH, new SettingItem(32, List.of("LIME_CONCRETE", "RED_CONCRETE")));
            }
        };

        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static final class SettingItem {
            private int slot = -1;
            private List<String> materials = new ArrayList<>();
        }
    }

    @Getter
    public static final class Clearchat implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private List<String> aliases = new ArrayList<>(List.of("clearchat"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Clearmail implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private List<String> aliases = new ArrayList<>(List.of("clearmail"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Coin implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private boolean draw = true;
        private int range = Range.PROXY;
        private List<String> aliases = new ArrayList<>(List.of("coin"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Dice implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int range = Range.PROXY;
        private int min = 1;
        private int max = 6;
        private List<String> aliases = new ArrayList<>(List.of("dice"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Do implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int range = Range.PROXY;
        private List<String> aliases = new ArrayList<>(List.of("do"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Flectonepulse implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private List<String> aliases = new ArrayList<>(){
            {
                add("flectonepulse");
                add("fp");
            }
        };
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Geolocate implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private boolean suggestOfflinePlayers = true;
        private List<String> aliases = new ArrayList<>(List.of("geolocate"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Helper implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int range = Range.PROXY;
        private List<String> aliases = new ArrayList<>(List.of("helper"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Ignore implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private boolean suggestOfflinePlayers = true;
        private List<String> aliases = new ArrayList<>(List.of("ignore"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Ignorelist implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int perPage = 4;
        private List<String> aliases = new ArrayList<>(List.of("ignorelist"));
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Kick implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int range = Range.PROXY;
        private List<String> aliases = new ArrayList<>(List.of("kick"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Mail implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private List<String> aliases = new ArrayList<>(List.of("mail"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Maintenance implements ISubCommand, ICommandFile {
        private boolean enable = true;
        @Setter
        public boolean turnedOn;
        private List<String> aliases = new ArrayList<>(List.of("maintenance"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Mark implements ISubCommand, ICommandFile {
        private boolean enable = false;
        private List<String> aliases = new ArrayList<>(List.of("mark"));
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Me implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int range = Range.PROXY;
        private List<String> aliases = new ArrayList<>(List.of("me"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Mute implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private boolean suggestOfflinePlayers = true;
        private int range = Range.PROXY;
        private List<String> aliases = new ArrayList<>(List.of("mute"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Mutelist implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int perPage = 4;
        private List<String> aliases = new ArrayList<>(List.of("mutelist"));
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Online implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private boolean suggestOfflinePlayers = true;
        private int range = Range.PROXY;
        private List<String> aliases = new ArrayList<>(List.of("online"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Ping implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private List<String> aliases = new ArrayList<>(List.of("ping"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Poll implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int range = Range.PROXY;
        @Setter
        private int lastId = 1;
        private int maxTime = 60000;
        private List<String> aliases = new ArrayList<>(List.of("poll"));
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Reply implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private List<String> aliases = new ArrayList<>(){
            {
                add("reply");
                add("r");
            }
        };
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Rockpaperscissors implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private List<String> aliases = new ArrayList<>(){
            {
                add("rockpaperscissors");
                add("rps");
            }
        };
        private Map<String, List<String>> strategies = new LinkedHashMap<>(){
            {
                put("rock", new ArrayList<>(List.of("scissors")));
                put("paper", new ArrayList<>(List.of("rock")));
                put("scissors", new ArrayList<>(List.of("paper")));
            }
        };
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Spit implements ISubCommand, ICommandFile {
        private boolean enable = false;
        private List<String> aliases = new ArrayList<>(List.of("spit"));
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Spy implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int range = Range.PROXY;
        private List<String> aliases = new ArrayList<>(List.of("spy"));
        private Map<String, List<String>> categories = new LinkedHashMap<>(){
            {
                put("command", List.of("tell"));
                put("action", List.of("anvil", "book", "sign", "chat"));
            }
        };
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Stream implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int range = Range.PROXY;
        private List<String> aliases = new ArrayList<>(List.of("stream"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound(true, 1f, 1f, SoundCategory.BLOCK.name(), Sounds.BLOCK_NOTE_BLOCK_BELL.getName().toString());
    }

    @Getter
    public static final class Symbol implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private List<String> aliases = new ArrayList<>(List.of("symbol"));
        private Map<String, String> categories = new LinkedHashMap<>(){
            {
                put("activities", "⚽ ⚾ ⛷ ⛸ ⛹ ⛺ \uD83C\uDF98 \uD83C\uDF99 \uD83C\uDFA4 \uD83C\uDF9E \uD83C\uDF9F \uD83C\uDFA0 \uD83C\uDFA1 \uD83C\uDFA2 \uD83C\uDFA5 \uD83C\uDFA6 \uD83C\uDFA7 \uD83C\uDFA8 \uD83C\uDFA9 \uD83C\uDFAA \uD83C\uDFAB \uD83C\uDFAC \uD83C\uDFAD \uD83C\uDFAE \uD83D\uDD79 \uD83C\uDFAF \uD83C\uDFB0 \uD83C\uDFB1 \uD83C\uDFB2 \uD83C\uDFB3 \uD83C\uDFB4 \uD83C\uDFBD \uD83C\uDFBE \uD83C\uDFBF \uD83C\uDFC0 \uD83C\uDFC1 \uD83C\uDFC2 \uD83C\uDFC3 \uD83C\uDFC4 \uD83C\uDFC7 \uD83C\uDFC8 \uD83C\uDFC9 \uD83C\uDFCA \uD83C\uDFCB \uD83C\uDFCC \uD83C\uDFCD \uD83C\uDFCE \uD83C\uDFCF \uD83C\uDFD0 \uD83C\uDFD1 \uD83C\uDFD2 \uD83C\uDFD3 \uD83E\uDD4A \uD83E\uDD4B \uD83E\uDD4C \uD83E\uDD4D \uD83E\uDD4E \uD83E\uDD4F \uD83E\uDE80 \uD83E\uDE81 \uD83E\uDE82 \uD83E\uDE83 \uD83E\uDE84 \uD83E\uDE85 \uD83E\uDE86 \uD83E\uDD3A \uD83E\uDD3B \uD83E\uDD3C \uD83E\uDD3D \uD83E\uDD3E \uD83E\uDD3F \uD83E\uDDD7 \uD83E\uDDD8 \uD83E\uDD33 \uD83E\uDD37 \uD83E\uDD38 \uD83E\uDD39 \uD83C\uDFD5 \uD83C\uDFD6 \uD83C\uDFF8 \uD83C\uDFF9 \uD83D\uDC92 \uD83C\uDF80 \uD83C\uDF81 \uD83E\uDDE7 \uD83C\uDF82 \uD83C\uDF83 \uD83C\uDF84 \uD83C\uDF85 \uD83C\uDF86 \uD83C\uDF87 \uD83C\uDF88 \uD83C\uDF89 \uD83C\uDF8A \uD83C\uDF8B \uD83C\uDF8C \uD83C\uDF8D \uD83C\uDF8E \uD83C\uDF8F \uD83C\uDF90 \uD83C\uDF91");
                put("animals", "\uD83D\uDC00 \uD83D\uDC01 \uD83D\uDC02 \uD83D\uDC03 \uD83D\uDC04 \uD83D\uDC05 \uD83D\uDC06 \uD83D\uDC07 \uD83D\uDC08 \uD83D\uDC09 \uD83D\uDC0A \uD83D\uDC0B \uD83D\uDC0C \uD83D\uDC0D \uD83D\uDC0E \uD83D\uDC0F \uD83D\uDC10 \uD83D\uDC11 \uD83D\uDC12 \uD83D\uDC13 \uD83D\uDC14 \uD83D\uDC15 \uD83D\uDC16 \uD83D\uDC17 \uD83D\uDC18 \uD83D\uDC19 \uD83D\uDC1A \uD83D\uDC1B \uD83D\uDC1C \uD83D\uDC1D \uD83D\uDC1E \uD83D\uDC1F \uD83D\uDC20 \uD83D\uDC21 \uD83D\uDC22 \uD83D\uDC23 \uD83D\uDC24 \uD83D\uDC25 \uD83D\uDC26 \uD83D\uDC27 \uD83D\uDC28 \uD83D\uDC29 \uD83D\uDC2A \uD83D\uDC2B \uD83D\uDC2C \uD83D\uDC2D \uD83D\uDC2E \uD83D\uDC2F \uD83D\uDC30 \uD83D\uDC31 \uD83D\uDC32 \uD83D\uDC33 \uD83D\uDC34 \uD83D\uDC35 \uD83D\uDC36 \uD83D\uDC37 \uD83D\uDC38 \uD83D\uDC39 \uD83D\uDC3A \uD83D\uDC3B \uD83D\uDC3C \uD83D\uDC3D \uD83D\uDC3E \uD83D\uDC3F \uD83D\uDD77 \uD83D\uDD78 \uD83E\uDD80 \uD83E\uDD81 \uD83E\uDD82 \uD83E\uDD83 \uD83E\uDD84 \uD83E\uDD85 \uD83E\uDD86 \uD83E\uDD87 \uD83E\uDD88 \uD83E\uDD89 \uD83E\uDD8A \uD83E\uDD8B \uD83E\uDD8C \uD83E\uDD8D \uD83E\uDD8E \uD83E\uDD8F \uD83E\uDD90 \uD83E\uDD91 \uD83E\uDD92 \uD83E\uDD93 \uD83E\uDD94 \uD83E\uDD95 \uD83E\uDD96 \uD83E\uDD97 \uD83E\uDD98 \uD83E\uDD99 \uD83E\uDD9A \uD83E\uDD9B \uD83E\uDD9C \uD83E\uDD9D \uD83E\uDD9E \uD83E\uDD9F \uD83E\uDDA0 \uD83E\uDDA1 \uD83E\uDDA2 \uD83E\uDDA3 \uD83E\uDDA4 \uD83E\uDDA5 \uD83E\uDDA6 \uD83E\uDDA7 \uD83E\uDDA8 \uD83E\uDDA9 \uD83E\uDDAA \uD83E\uDDAB \uD83E\uDDAC \uD83E\uDDAD \uD83E\uDDAE \uD83E\uDEB0 \uD83E\uDEB1 \uD83E\uDEB2 \uD83E\uDEB3 \uD83E\uDEB9 \uD83E\uDEBA \uD83E\uDEBC \uD83E\uDEBF \uD83E\uDECE \uD83E\uDECF");
                put("arrows", "\uD83E\uDC60 \uD83E\uDC61 \uD83E\uDC62 \uD83E\uDC63 \uD83E\uDC64 \uD83E\uDC65 \uD83E\uDC66 \uD83E\uDC67 \uD83E\uDC68 \uD83E\uDC69 \uD83E\uDC6A \uD83E\uDC6B \uD83E\uDC6C \uD83E\uDC6D \uD83E\uDC6E \uD83E\uDC6F \uD83E\uDC80 \uD83E\uDC81 \uD83E\uDC82 \uD83E\uDC83 \uD83E\uDC84 \uD83E\uDC85 \uD83E\uDC86 \uD83E\uDC87 ← ↑ → ↓ ↔ ⇄ ⇵ ⇏ ⇒ ⇔ \uD83E\uDC18 \uD83E\uDC19 \uD83E\uDC1A \uD83E\uDC1B \uD83D\uDD19 \uD83D\uDD1A \uD83D\uDD1B \uD83D\uDD1C \uD83D\uDD1D \uD83D\uDDD8");
                put("body", "\uD83D\uDC40 \uD83D\uDC41 \uD83D\uDC42 \uD83E\uDDBB \uD83D\uDC43 \uD83D\uDC44 \uD83D\uDC45 \uD83D\uDC8B \uD83D\uDDE2 \uD83D\uDCAA \uD83E\uDDB4 \uD83E\uDDB5 \uD83E\uDDB6 \uD83E\uDDB7 \uD83E\uDDBE \uD83E\uDDBF \uD83E\uDDE0 \uD83E\uDEC0 \uD83E\uDEC1 \uD83E\uDEE6");
                put("clothes", "\uD83D\uDC51 \uD83D\uDC52 \uD83D\uDC53 \uD83D\uDD76 \uD83D\uDC54 \uD83D\uDC55 \uD83D\uDC56 \uD83D\uDC57 \uD83D\uDC58 \uD83D\uDC59 \uD83D\uDC5A \uD83D\uDC5B \uD83D\uDC5C \uD83D\uDC5D \uD83D\uDC5E \uD83D\uDC5F \uD83D\uDC60 \uD83D\uDC61 \uD83D\uDC62 \uD83E\uDD7B \uD83E\uDD7C \uD83E\uDD7D \uD83E\uDD7E \uD83E\uDD7F \uD83E\uDDBA \uD83E\uDDAF \uD83E\uDDE2 \uD83E\uDDE3 \uD83E\uDDE4 \uD83E\uDDE5 \uD83E\uDDE6 \uD83E\uDE70 \uD83E\uDE71 \uD83E\uDE72 \uD83E\uDE73 \uD83E\uDE74 \uD83E\uDE96");
                put("environment", "☼ ☽ ☾ ☀ \uD83C\uDF04 \uD83C\uDF05 ☁ ☂ ☔ \uD83C\uDF02 ☄ ★ ☆ ⛅ ⛆ ☃ ⛄ ⛇ ⛈ ❄ \uD83C\uDF00 \uD83C\uDF01 \uD83C\uDF08 \uD83C\uDF0A \uD83C\uDF0B \uD83C\uDF22 \uD83D\uDCA7 \uD83C\uDF23 \uD83C\uDF24 \uD83C\uDF25 \uD83C\uDF26 \uD83C\uDF27 \uD83C\uDF28 \uD83C\uDF29 \uD83C\uDF2A \uD83C\uDF2B \uD83C\uDF2C \uD83C\uDF06 \uD83C\uDF07 \uD83C\uDF09 ⭐ ⭑ ⭒ ⯪ ⯫ \uD83D\uDD2F \uD83C\uDF03 \uD83C\uDF0C \uD83C\uDF1E \uD83C\uDF1F \uD83C\uDF20 \uD83C\uDF0D \uD83C\uDF0E \uD83C\uDF0F \uD83C\uDF10 \uD83D\uDDFA \uD83E\uDE90 \uD83C\uDF11 \uD83C\uDF12 \uD83C\uDF13 \uD83C\uDF14 \uD83C\uDF15 \uD83C\uDF16 \uD83C\uDF17 \uD83C\uDF18 \uD83C\uDF19 \uD83C\uDF1A \uD83C\uDF1B \uD83C\uDF1C \uD83C\uDF1D \uD83C\uDFD4 \uD83C\uDFDC \uD83C\uDFDD \uD83C\uDFDE");
                put("faces", "☹ ☺ ☻ \uD83D\uDE00 \uD83D\uDE01 \uD83D\uDE02 \uD83D\uDE03 \uD83D\uDE04 \uD83D\uDE05 \uD83D\uDE06 \uD83D\uDE07 \uD83D\uDE08 \uD83D\uDE09 \uD83D\uDE0A \uD83D\uDE0B \uD83D\uDE0C \uD83D\uDE0D \uD83D\uDE0E \uD83D\uDE0F \uD83D\uDE10 \uD83D\uDE11 \uD83D\uDE12 \uD83D\uDE13 \uD83D\uDE14 \uD83D\uDE15 \uD83D\uDE16 \uD83D\uDE17 \uD83D\uDE18 \uD83D\uDE19 \uD83D\uDE1A \uD83D\uDE1B \uD83D\uDE1C \uD83D\uDE1D \uD83D\uDE1E \uD83D\uDE1F \uD83D\uDE20 \uD83D\uDE21 \uD83D\uDE22 \uD83D\uDE23 \uD83D\uDE24 \uD83D\uDE25 \uD83D\uDE26 \uD83D\uDE27 \uD83D\uDE28 \uD83D\uDE29 \uD83D\uDE2A \uD83D\uDE2B \uD83D\uDE2C \uD83D\uDE2D \uD83D\uDE2E \uD83D\uDE2F \uD83D\uDE30 \uD83D\uDE31 \uD83D\uDE32 \uD83D\uDE33 \uD83D\uDE34 \uD83D\uDE35 \uD83D\uDE36 \uD83D\uDE37 \uD83D\uDE38 \uD83D\uDE39 \uD83D\uDE3A \uD83D\uDE3B \uD83D\uDE3C \uD83D\uDE3D \uD83D\uDE3E \uD83D\uDE3F \uD83D\uDE40 \uD83D\uDE41 \uD83D\uDE42 \uD83D\uDE43 \uD83D\uDE44 \uD83D\uDE45 \uD83D\uDE46 \uD83D\uDE47 \uD83D\uDE48 \uD83D\uDE49 \uD83D\uDE4A \uD83D\uDE4B \uD83D\uDE4C \uD83D\uDE4D \uD83D\uDE4E \uD83D\uDE4F \uD83E\uDD10 \uD83E\uDD11 \uD83E\uDD12 \uD83E\uDD13 \uD83E\uDD14 \uD83E\uDD15 \uD83E\uDD17 \uD83E\uDD20 \uD83E\uDD21 \uD83E\uDD22 \uD83E\uDD23 \uD83E\uDD24 \uD83E\uDD25 \uD83E\uDD26 \uD83E\uDD27 \uD83E\uDD28 \uD83E\uDD29 \uD83E\uDD2A \uD83E\uDD2B \uD83E\uDD2C \uD83E\uDD2D \uD83E\uDD2E \uD83E\uDD2F \uD83E\uDD70 \uD83E\uDD71 \uD83E\uDD72 \uD83E\uDD73 \uD83E\uDD74 \uD83E\uDD75 \uD83E\uDD76 \uD83E\uDD77 \uD83E\uDD78 \uD83E\uDD79 \uD83E\uDD7A \uD83E\uDDD0 \uD83E\uDEE0 \uD83E\uDEE1 \uD83E\uDEE2 \uD83E\uDEE3 \uD83E\uDEE4 \uD83E\uDEE5 \uD83E\uDEE8");
                put("food", "☕ \uD83C\uDF2D \uD83C\uDF2E \uD83C\uDF2F \uD83C\uDF30 \uD83C\uDF36 \uD83C\uDF45 \uD83C\uDF46 \uD83C\uDF47 \uD83C\uDF48 \uD83C\uDF49 \uD83C\uDF4A \uD83C\uDF4B \uD83C\uDF4C \uD83C\uDF4D \uD83C\uDF4E \uD83C\uDF4F \uD83C\uDF50 \uD83C\uDF51 \uD83C\uDF52 \uD83C\uDF53 \uD83C\uDF54 \uD83C\uDF55 \uD83C\uDF56 \uD83C\uDF57 \uD83C\uDF58 \uD83C\uDF59 \uD83C\uDF5A \uD83C\uDF5B \uD83C\uDF5C \uD83C\uDF5D \uD83C\uDF5E \uD83C\uDF5F \uD83C\uDF60 \uD83C\uDF61 \uD83C\uDF62 \uD83C\uDF63 \uD83C\uDF64 \uD83C\uDF65 \uD83C\uDF66 \uD83C\uDF67 \uD83C\uDF68 \uD83C\uDF69 \uD83C\uDF6A \uD83C\uDF6B \uD83C\uDF6C \uD83C\uDF6D \uD83C\uDF6E \uD83C\uDF6F \uD83C\uDF70 \uD83C\uDF71 \uD83C\uDF72 \uD83C\uDF73 \uD83C\uDF74 \uD83C\uDF75 \uD83C\uDF76 \uD83C\uDF77 \uD83C\uDF78 \uD83C\uDF79 \uD83C\uDF7A \uD83C\uDF7B \uD83C\uDF7C \uD83C\uDF7D \uD83C\uDF7E \uD83C\uDF7F \uD83E\uDD50 \uD83E\uDD51 \uD83E\uDD52 \uD83E\uDD53 \uD83E\uDD54 \uD83E\uDD55 \uD83E\uDD56 \uD83E\uDD57 \uD83E\uDD58 \uD83E\uDD59 \uD83E\uDD5A \uD83E\uDD5B \uD83E\uDD5C \uD83E\uDD5D \uD83E\uDD5E \uD83E\uDD5F \uD83E\uDD60 \uD83E\uDD61 \uD83E\uDD62 \uD83E\uDD63 \uD83E\uDD64 \uD83E\uDD65 \uD83E\uDD66 \uD83E\uDD67 \uD83E\uDD68 \uD83E\uDD69 \uD83E\uDD6A \uD83E\uDD6B \uD83E\uDD6C \uD83E\uDD6D \uD83E\uDD6E \uD83E\uDD6F \uD83E\uDDC0 \uD83E\uDDC1 \uD83E\uDDC2 \uD83E\uDDC3 \uD83E\uDDC4 \uD83E\uDDC5 \uD83E\uDDC6 \uD83E\uDDC7 \uD83E\uDDC8 \uD83E\uDDC9 \uD83E\uDDCA \uD83E\uDDCB \uD83E\uDED0 \uD83E\uDED1 \uD83E\uDED2 \uD83E\uDED3 \uD83E\uDED4 \uD83E\uDED5 \uD83E\uDED6 \uD83E\uDED7 \uD83E\uDED8 \uD83E\uDED9 \uD83E\uDEDA \uD83E\uDEDB");
                put("greenery", "\uD83C\uDF31 \uD83C\uDF32 \uD83C\uDF33 \uD83C\uDF34 \uD83C\uDF35 \uD83C\uDF37 \uD83C\uDF38 \uD83C\uDF39 \uD83E\uDD40 \uD83D\uDCAE \uD83C\uDF3A \uD83C\uDF3B \uD83C\uDF3C \uD83C\uDF3D \uD83C\uDF3E \uD83C\uDF3F \uD83C\uDF40 \uD83C\uDF41 \uD83C\uDF42 \uD83C\uDF43 \uD83C\uDF44 \uD83C\uDFF5 \uD83C\uDFF6 \uD83E\uDEB4 \uD83E\uDEB7 \uD83E\uDEB8 \uD83E\uDEBB");
                put("hands", "☜ ☝ ☞ ☟ ✊ ✋ ✌ ✍ \uD83D\uDC46 \uD83D\uDC47 \uD83D\uDC48 \uD83D\uDC49 \uD83D\uDC4A \uD83D\uDC4B \uD83D\uDC4C \uD83D\uDC4D \uD83D\uDC4E \uD83D\uDC4F \uD83D\uDC50 \uD83D\uDD8E \uD83D\uDD8F \uD83D\uDD90 \uD83D\uDD91 \uD83D\uDD92 \uD83D\uDD93 \uD83D\uDD94 \uD83D\uDD95 \uD83D\uDD96 \uD83D\uDD97 \uD83D\uDD98 \uD83D\uDD99 \uD83D\uDD9A \uD83D\uDD9B \uD83D\uDD9C \uD83D\uDD9D \uD83D\uDD9E \uD83D\uDD9F \uD83D\uDDA0 \uD83D\uDDA1 \uD83D\uDDA2 \uD83D\uDDA3 \uD83E\uDD0C \uD83E\uDD0F \uD83E\uDD18 \uD83E\uDD19 \uD83E\uDD1A \uD83E\uDD1B \uD83E\uDD1C \uD83E\uDD1D \uD83E\uDD1E \uD83E\uDD1F \uD83E\uDD32 \uD83E\uDEF0 \uD83E\uDEF1 \uD83E\uDEF2 \uD83E\uDEF3 \uD83E\uDEF4 \uD83E\uDEF5 \uD83E\uDEF6 \uD83E\uDEF7 \uD83E\uDEF8");
                put("misc", "⏏ ⏩ ⏪ ⏫ ⏬ ⏭ ⏮ ⏯ ⏰ ⏱ ⏲ ⏳ ⏴ ⏵ ⏶ ⏷ ⏸ ⏹ ⏺ ⏻ ⏼ ⌚ ⌛ \uD83D\uDD50 \uD83D\uDD51 \uD83D\uDD52 \uD83D\uDD53 \uD83D\uDD54 \uD83D\uDD55 \uD83D\uDD56 \uD83D\uDD57 \uD83D\uDD58 \uD83D\uDD59 \uD83D\uDD5A \uD83D\uDD5B \uD83D\uDD5C \uD83D\uDD5D \uD83D\uDD5E \uD83D\uDD5F \uD83D\uDD60 \uD83D\uDD61 \uD83D\uDD62 \uD83D\uDD63 \uD83D\uDD64 \uD83D\uDD65 \uD83D\uDD66 \uD83D\uDD67 \uD83D\uDD05 \uD83D\uDD06 \uD83D\uDD07 \uD83D\uDD08 \uD83D\uDD09 \uD83D\uDD0A \uD83D\uDD68 \uD83D\uDD69 \uD83D\uDD6A \uD83D\uDD6B \uD83D\uDD6C \uD83D\uDD6D \uD83D\uDD0B \uD83E\uDEAB \uD83D\uDD0C \uD83D\uDD0D \uD83D\uDD0E \uD83D\uDEDC \uD83D\uDDD4 \uD83D\uDDD5 \uD83D\uDDD6 \uD83D\uDDD7 \uD83D\uDDD8 \uD83D\uDDD9 \uD83D\uDDDA \uD83D\uDDDB ✉ \uD83D\uDC8C \uD83D\uDCE7 \uD83D\uDCE8 \uD83D\uDCE9 \uD83D\uDD82 \uD83D\uDD83 \uD83D\uDD84 \uD83D\uDD85 \uD83D\uDD86 \uD83E\uDEAA ⚐ ⚑ ⚕ ⚖ ⚗ ⚘ ⚙ ⛭ ⛮ ⚠ ⚡ ⚰ ⚱ ♔ ♕ ♖ ♗ ♘ ♙ ♚ ♛ ♜ ♝ ♞ ♟ ♠ ♡ ♢ ♣ ♤ ♥ ♦ ♧ ⚀ ⚁ ⚂ ⚃ ⚄ ⚅ ⛀ ⛁ ⛂ ⛃ \uD83D\uDCA0 \uD83D\uDD30 \uD83D\uDCA2 \uD83D\uDCA4 \uD83D\uDCA5 \uD83D\uDD25 \uD83D\uDCA6 \uD83D\uDCA8 \uD83E\uDEE7 \uD83D\uDCAB \uD83D\uDCAC \uD83D\uDCAD \uD83D\uDDE8 \uD83D\uDDE9 \uD83D\uDDEA \uD83D\uDDEB \uD83D\uDDEC \uD83D\uDDED \uD83D\uDDEE \uD83D\uDDEF \uD83D\uDDF0 \uD83D\uDDF1 \uD83D\uDDF2 \uD83D\uDCB1 \uD83D\uDCB2 \uD83D\uDCB3 \uD83D\uDCB4 \uD83D\uDCB5 \uD83D\uDCB6 \uD83D\uDCB7 \uD83D\uDCB8 \uD83E\uDE99 \uD83D\uDCB9 \uD83D\uDCC8 \uD83D\uDCC9 \uD83D\uDCCA \uD83D\uDDE0");
                put("numbers", "\uD83E\uDFF0 \uD83E\uDFF1 \uD83E\uDFF2 \uD83E\uDFF3 \uD83E\uDFF4 \uD83E\uDFF5 \uD83E\uDFF6 \uD83E\uDFF7 \uD83E\uDFF8 \uD83E\uDFF9 ½ ↉ ⅓ ⅔ ¼ ¾ ⅕ ⅖ ⅗ ⅘ ⅙ ⅚ ⅐ ⅛ ⅜ ⅝ ⅞ ⅑ ⅒ ⅟ ⓪ ① ② ③ ④ ⑤ ⑥ ⑦ ⑧ ⑨ ⑩ ⑪ ⑫ ⑬ ⑭ ⑮ ⑯ ⑰ ⑱ ⑲ ⑳ ⓿ ➊ ➋ ➌ ➍ ➎ ➏ ➐ ➑ ➒ ➓ Ⅰ Ⅱ Ⅲ Ⅳ Ⅴ Ⅵ Ⅶ Ⅷ Ⅸ Ⅹ Ⅺ Ⅻ Ⅼ Ⅽ Ⅾ Ⅿ");
                put("people", "\uD83D\uDC63 \uD83D\uDC64 \uD83D\uDC65 \uD83D\uDC66 \uD83D\uDC67 \uD83D\uDC68 \uD83D\uDC69 \uD83E\uDDD1 \uD83E\uDDD2 \uD83E\uDDD3 \uD83E\uDDD4 \uD83E\uDDD5 \uD83D\uDC6A \uD83D\uDC6B \uD83D\uDC6C \uD83D\uDC6D \uD83E\uDDCD \uD83E\uDDCE \uD83D\uDEB9 \uD83D\uDEBA \uD83D\uDEBC \uD83D\uDEC9 \uD83D\uDECA \uD83E\uDDCF \uD83D\uDC6E \uD83D\uDC6F \uD83D\uDC70 \uD83D\uDC71 \uD83D\uDC72 \uD83E\uDD36 \uD83D\uDC73 \uD83D\uDC74 \uD83D\uDC75 \uD83D\uDC76 \uD83D\uDC77 \uD83D\uDC78 \uD83D\uDC79 \uD83D\uDC7A \uD83D\uDC7C \uD83E\uDD30 \uD83E\uDEC2 \uD83E\uDEC3 \uD83E\uDEC4 \uD83E\uDD31 \uD83E\uDDD6 \uD83D\uDC7B \uD83D\uDC7D \uD83D\uDC7E \uD83E\uDD16 \uD83E\uDDCC \uD83D\uDC7F ☠ \uD83D\uDC80 \uD83D\uDD71 \uD83D\uDC81 \uD83D\uDC82 \uD83E\uDD34 \uD83E\uDEC5 \uD83E\uDD35 \uD83D\uDC83 \uD83D\uDD7A \uD83D\uDC86 \uD83D\uDC87 \uD83D\uDC8F \uD83D\uDC91 \uD83D\uDDE3 \uD83E\uDDB8 \uD83E\uDDB9 \uD83D\uDD74 \uD83D\uDD75 \uD83E\uDDD9 \uD83E\uDDDA \uD83E\uDDDB \uD83E\uDDDC \uD83E\uDDDD \uD83E\uDDDE \uD83E\uDDDF");
                put("shapes", "\uD83D\uDD32 \uD83D\uDD33 \uD83D\uDD34 \uD83D\uDD35 \uD83D\uDD36 \uD83D\uDD37 \uD83D\uDD38 \uD83D\uDD39 \uD83D\uDD3A \uD83D\uDD3B \uD83D\uDD3C \uD83D\uDD3D \uD83D\uDC93 \uD83D\uDC94 \uD83D\uDC95 \uD83D\uDC96 \uD83D\uDC97 \uD83D\uDC98 \uD83D\uDC99 \uD83D\uDC9A \uD83D\uDC9B \uD83D\uDC9C \uD83D\uDDA4 \uD83D\uDC9D \uD83D\uDC9E \uD83D\uDC9F \uD83E\uDD0D \uD83E\uDD0E \uD83E\uDDE1 \uD83E\uDE75 \uD83E\uDE76 \uD83E\uDE77 ❣ ❤ ❥ ❦ ❧ \uD83C\uDF94 │ ┤ ╡ ╢ ╖ ╕ ╣ ║ ╗ ╝ ╜ ╛ ┐ └ ┴ ┬ ├ ─ ┼ ╞ ╟ ╚ ╔ ╩ ╦ ╠ ═ ╬ ╧ ╨ ╤ ╥ ╙ ╘ ╒ ╓ ╫ ╪ ┘ ┌ ▁ ▂ ▃ ▄ ▅ ▆ ▇ █ ▉ ▊ ▋ ▌ ▍ ▎ ▏ ▕ ▐ ▔ ▀ ▝ ▖ ▗ ▘ ▙ ▛ ▜ ▟ ▞ ▚");
                put("symbols", "♩ ♪ ♫ ♬ ♭ ♮ ♯ \uD83C\uDF9C \uD83C\uDF9D \uD83C\uDFB5 \uD83C\uDFB6 \uD83C\uDFBC ♲ ♻ ☮ ☯ \uD83D\uDD46 \uD83D\uDD47 \uD83D\uDD48 \uD83D\uDD49 \uD83D\uDD4A \uD83D\uDD4E \uD83D\uDD4F ☐ ☑ ☒ ✓ ✔ ✕ ✖ ✗ ✘ ❌ ❎ \uD83D\uDDF4 \uD83D\uDDF5 \uD83D\uDDF6 \uD83D\uDDF7 \uD83D\uDDF8 \uD83D\uDDF9 ♿ \uD83D\uDD1E ⛔ \uD83D\uDD72 \uD83D\uDEAB \uD83D\uDEAC \uD83D\uDEAD \uD83D\uDEAE \uD83D\uDEAF \uD83D\uDEB0 \uD83D\uDEB1 \uD83D\uDEC6 \uD83D\uDEC7 \uD83D\uDEC8 \uD83D\uDED1 \uD83D\uDD1F \uD83D\uDD20 \uD83D\uDD21 \uD83D\uDD22 \uD83D\uDD23 \uD83D\uDD24 \uD83C\uDD91 \uD83C\uDD92 \uD83C\uDD93 \uD83C\uDD94 \uD83C\uDD95 \uD83C\uDD96 \uD83C\uDD97 \uD83C\uDD98 \uD83C\uDD99 \uD83C\uDD9A ❓ ❔ ❕ ❗ ❛ ❜ ❝ ❞ ❟ ❠ \uD83D\uDCAF \uD83C\uDFF1 \uD83C\uDFF2 \uD83C\uDFF3 \uD83C\uDFF4 \uD83D\uDEA9");
                put("things", "⛏ \uD83E\uDE93 ⚒ ⚓ ⚔ \uD83C\uDFA3 \uD83D\uDDE1 \uD83D\uDD31 ⛓ \uD83D\uDEE0 \uD83D\uDEE1 \uD83E\uDE9A \uD83E\uDE9B ✀ ✁ ✂ ✃ ✄ ✎ ✏ ✐ ✑ ✒ \uD83D\uDD89 \uD83D\uDD8A \uD83D\uDD8B \uD83D\uDD8C \uD83D\uDD8D \uD83C\uDF21 \uD83C\uDFF7 \uD83C\uDF92 \uD83C\uDF93 \uD83C\uDF95 \uD83D\uDC90 \uD83C\uDF96 \uD83C\uDF97 \uD83C\uDFC5 \uD83E\uDD47 \uD83E\uDD48 \uD83E\uDD49 \uD83C\uDFC6 \uD83C\uDF9A \uD83C\uDF9B \uD83C\uDFB7 \uD83C\uDFB8 \uD83C\uDFB9 \uD83C\uDFBA \uD83C\uDFBB \uD83E\uDE87 \uD83E\uDE88 \uD83E\uDE95 \uD83E\uDE97 \uD83E\uDE98 \uD83C\uDFFA \uD83D\uDC84 \uD83D\uDC85 \uD83E\uDEA8 \uD83E\uDEB5 \uD83E\uDEB6 \uD83E\uDEBD \uD83D\uDC88 \uD83D\uDC89 \uD83D\uDC8A \uD83E\uDDEA \uD83E\uDDEB \uD83E\uDE78 \uD83E\uDE79 \uD83E\uDE7A \uD83E\uDE7B \uD83E\uDE7C \uD83E\uDDEC \uD83E\uDDED \uD83E\uDDEE \uD83E\uDDEF \uD83E\uDDF0 \uD83E\uDDF1 \uD83E\uDDF2 \uD83E\uDDF3 \uD83E\uDDF4 \uD83E\uDDF5 \uD83E\uDDF6 \uD83E\uDDF7 \uD83E\uDDF8 \uD83E\uDDF9 \uD83E\uDDFA \uD83E\uDDFB \uD83E\uDDFC \uD83E\uDDFD \uD83E\uDDFE \uD83E\uDDFF \uD83D\uDC8D \uD83D\uDC8E \uD83D\uDCA1 \uD83D\uDCA3 \uD83E\uDDE8 \uD83E\uDD41 \uD83E\uDD42 \uD83E\uDD43 \uD83E\uDD44 \uD83E\uDD45 \uD83E\uDD46 \uD83E\uDDE9 \uD83E\uDEA0 \uD83E\uDEA1 \uD83E\uDEA2 \uD83E\uDEA3 \uD83E\uDEA4 \uD83E\uDEA5 \uD83E\uDEA6 \uD83E\uDEA7 \uD83D\uDCA9 \uD83D\uDCB0 \uD83D\uDCBA \uD83D\uDCBB \uD83D\uDCBC \uD83D\uDCBD \uD83D\uDDAD \uD83D\uDDB4 \uD83D\uDCBE \uD83D\uDDAA \uD83D\uDDAB \uD83D\uDDAC \uD83D\uDCBF \uD83D\uDCC0 \uD83D\uDDB8 \uD83D\uDCC1 \uD83D\uDCC2 \uD83D\uDDBF \uD83D\uDDC0 \uD83D\uDDC1 \uD83D\uDDC2 \uD83D\uDDC3 \uD83D\uDDC4 \uD83D\uDCC3 \uD83D\uDCC4 \uD83D\uDCC5 \uD83D\uDCC6 \uD83D\uDCC7 \uD83D\uDDB9 \uD83D\uDDBA \uD83D\uDDBB \uD83D\uDDBC \uD83D\uDDBD \uD83D\uDDBE \uD83D\uDCCB \uD83D\uDCCC \uD83D\uDCCD \uD83D\uDD88 \uD83D\uDCCE \uD83D\uDD87 \uD83D\uDCCF \uD83D\uDCD0 \uD83D\uDCD1 \uD83D\uDCD2 \uD83D\uDCD3 \uD83D\uDCD4 \uD83D\uDCD5 \uD83D\uDCD6 \uD83D\uDCD7 \uD83D\uDCD8 \uD83D\uDCD9 \uD83D\uDCDA \uD83D\uDCDB \uD83D\uDCDC \uD83D\uDCDD \uD83D\uDDC5 \uD83D\uDDC6 \uD83D\uDDC7 \uD83D\uDDC8 \uD83D\uDDC9 \uD83D\uDDCA \uD83D\uDDCB \uD83D\uDDCC \uD83D\uDDCD \uD83D\uDDCE \uD83D\uDDCF \uD83D\uDDD0 \uD83D\uDDD1 \uD83D\uDDD2 \uD83D\uDDD3 \uD83D\uDCDE \uD83D\uDCDF \uD83D\uDCE0 \uD83D\uDCE1 \uD83D\uDCE2 \uD83D\uDCE3 \uD83D\uDCE4 \uD83D\uDCE5 \uD83D\uDCE6 \uD83D\uDCEA \uD83D\uDCEB \uD83D\uDCEC \uD83D\uDCED \uD83D\uDCEE \uD83D\uDCEF \uD83D\uDCF0 \uD83D\uDCF1 \uD83D\uDCF2 \uD83D\uDCF3 \uD83D\uDCF4 \uD83D\uDCF5 \uD83D\uDCF6 \uD83D\uDD7B \uD83D\uDD7C \uD83D\uDD7D \uD83D\uDD7E \uD83D\uDD7F \uD83D\uDD80 \uD83D\uDD81 \uD83D\uDCF7 \uD83D\uDCF8 \uD83D\uDCF9 \uD83D\uDCFA \uD83D\uDCFB \uD83D\uDCFC \uD83D\uDCFD \uD83D\uDCFE \uD83D\uDCFF \uD83D\uDDA5 \uD83D\uDDA6 \uD83D\uDDA7 \uD83D\uDDA8 \uD83D\uDDA9 \uD83D\uDDAE \uD83D\uDDAF \uD83D\uDDB0 \uD83D\uDDB1 \uD83D\uDDB2 \uD83D\uDDB3 \uD83D\uDDB5 \uD83D\uDDB6 \uD83D\uDDB7 \uD83D\uDD0F \uD83D\uDD10 \uD83D\uDD11 \uD83D\uDD12 \uD83D\uDD13 \uD83D\uDD14 \uD83D\uDD15 \uD83D\uDD16 \uD83D\uDD17 \uD83D\uDD18 \uD83D\uDD26 \uD83D\uDD27 \uD83D\uDD28 \uD83D\uDD29 \uD83D\uDD2A \uD83D\uDD2B \uD83D\uDD2C \uD83D\uDD2D \uD83D\uDD2E \uD83E\uDE92 \uD83E\uDE94 \uD83D\uDD6E \uD83D\uDD6F \uD83D\uDD70 \uD83D\uDDF3 \uD83D\uDECD \uD83D\uDECE \uD83D\uDEE2 \uD83D\uDDDC \uD83D\uDDDD \uD83D\uDDDE \uD83D\uDDDF \uD83D\uDD73");
                put("transport", "⛟ ⛴ ⛵ ✈ ⛩ ⛪ ⛫ ⛰ ⛱ ⛲ ⛳ ⛽ \uD83D\uDDFB \uD83D\uDDFC \uD83D\uDDFD \uD83D\uDDFE \uD83D\uDDFF \uD83D\uDE80 \uD83D\uDE81 \uD83D\uDE82 \uD83D\uDE83 \uD83D\uDE84 \uD83D\uDE85 \uD83D\uDE86 \uD83D\uDE87 \uD83D\uDE88 \uD83D\uDE89 \uD83D\uDE8A \uD83D\uDEF2 \uD83D\uDE8B \uD83D\uDE8C \uD83D\uDE8D \uD83D\uDE8E \uD83D\uDE8F \uD83D\uDE90 \uD83D\uDE91 \uD83D\uDE92 \uD83D\uDEF1 \uD83D\uDE93 \uD83D\uDE94 \uD83D\uDE95 \uD83D\uDE96 \uD83D\uDE97 \uD83D\uDEFB \uD83D\uDE98 \uD83D\uDE99 \uD83D\uDE9A \uD83D\uDE9B \uD83D\uDE9C \uD83D\uDE9D \uD83D\uDE9E \uD83D\uDE9F \uD83D\uDEA0 \uD83D\uDEA1 \uD83D\uDEA2 \uD83D\uDEF3 \uD83D\uDEA3 \uD83D\uDEA4 \uD83D\uDEF6 \uD83D\uDEE5 \uD83D\uDEE6 \uD83D\uDEE7 \uD83D\uDEE8 \uD83D\uDEE9 \uD83D\uDEEA \uD83D\uDEEB \uD83D\uDEEC \uD83D\uDEF0 \uD83D\uDEF4 \uD83D\uDEF5 \uD83D\uDEF9 \uD83D\uDEFC \uD83D\uDEFA \uD83D\uDEF7 \uD83E\uDDBC \uD83E\uDDBD \uD83D\uDEF8 \uD83D\uDED2 \uD83D\uDEA5 \uD83D\uDEA6 \uD83D\uDEA7 \uD83D\uDEA8 \uD83C\uDFD7 \uD83C\uDFD8 \uD83C\uDFD9 \uD83C\uDFDA \uD83C\uDFDB \uD83C\uDFDF \uD83C\uDFE0 \uD83C\uDFE1 \uD83C\uDFE2 \uD83C\uDFE3 \uD83C\uDFE4 \uD83C\uDFE5 \uD83C\uDFE6 \uD83C\uDFE7 \uD83C\uDFE8 \uD83C\uDFE9 \uD83C\uDFEA \uD83C\uDFEB \uD83C\uDFEC \uD83C\uDFED \uD83C\uDFEE \uD83C\uDFEF \uD83C\uDFF0 \uD83D\uDD4B \uD83D\uDD4C \uD83D\uDD4D \uD83D\uDED0 \uD83D\uDED3 \uD83D\uDED4 \uD83D\uDED5 \uD83D\uDED6 \uD83D\uDEB2 \uD83D\uDEB3 \uD83D\uDEB4 \uD83D\uDEB5 \uD83D\uDEB6 \uD83D\uDEB7 \uD83D\uDEB8 \uD83D\uDEAA \uD83E\uDE91 \uD83E\uDE9F \uD83E\uDE9C \uD83E\uDE9D \uD83E\uDE9E \uD83D\uDEBB \uD83D\uDEBD \uD83D\uDEBE \uD83D\uDEBF \uD83D\uDEC0 \uD83D\uDEC1 \uD83D\uDEC2 \uD83D\uDEC3 \uD83D\uDEC4 \uD83D\uDEC5 \uD83D\uDECB \uD83D\uDECC \uD83D\uDECF \uD83D\uDED7 \uD83D\uDEDD \uD83D\uDEDE \uD83D\uDEDF \uD83D\uDEE3 \uD83D\uDEE4");
            }
        };
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Tell implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private boolean suggestOfflinePlayers = false;
        private List<String> aliases = new ArrayList<>(){
            {
                add("tell");
                add("msg");
                add("w");
                add("message");
                add("send");
                add("m");
            }
        };
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Tictactoe implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private List<String> aliases = new ArrayList<>(){
            {
                add("tictactoe");
                add("ttt");
            }
        };
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Translateto implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int range = Range.PROXY;
        private List<String> aliases = new ArrayList<>(List.of("translateto"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Try implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int range = Range.PROXY;
        private int min = 0;
        private int max = 100;
        private int good = 50;
        private List<String> aliases = new ArrayList<>(List.of("try"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Unban implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int range = Range.PROXY;
        private List<String> aliases = new ArrayList<>(){
            {
                add("unban");
                add("pardon");
            }
        };
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Unmute implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int range = Range.PROXY;
        private List<String> aliases = new ArrayList<>(List.of("unmute"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Unwarn implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int range = Range.PROXY;
        private List<String> aliases = new ArrayList<>(List.of("unwarn"));
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Warn implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private boolean suggestOfflinePlayers = true;
        private int range = Range.PROXY;
        private List<String> aliases = new ArrayList<>(List.of("warn"));
        private Map<Integer, String> actions = new LinkedHashMap<>(){
            {
                put(2, "mute <target> 1h");
                put(3, "ban <target> 1h");
                put(5, "ban <target>");
            }
        };
        private Destination destination = new Destination();
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Warnlist implements ISubCommand, ICommandFile {
        private boolean enable = true;
        private int perPage = 4;
        private List<String> aliases = new ArrayList<>(List.of("warnlist"));
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    public interface ICommandFile extends Config.IEnable {
        List<String> getAliases();
    }
}
