package net.flectone.pulse.config;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"FieldMayBeFinal", "unused"})
@Getter
public final class Localization extends YamlFile implements ModuleConfig {

    public static final String FOLDER_NAME = "localizations";

    @JsonIgnore
    private final String language;

    @Setter
    @JsonIgnore
    private boolean initialized;

    public Localization(Path projectPath, String language) {
        super(Paths.get(projectPath.toString(), FOLDER_NAME, language + ".yml"));

        this.language = language;
    }

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/")
    String cooldown;

    Time time = new Time();

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/")
    Command command = new Command();

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/")
    Integration integration = new Integration();

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/")
    Message message = new Message();

    @Getter
    public static final class Time {
        String format;
        String permanent;
        String zero;
    }

    @Getter
    @Setter
    public static final class Command implements CommandConfig, Localizable {

        Exception exception = new Exception();

        Prompt prompt = new Prompt();

        @Override
        public SubCommandConfig getAfk() {
            return null;
        }

        @Getter
        @NoArgsConstructor
        public static final class Exception {
            String execution;
            String parseUnknown;
            String parseBoolean;
            String parseNumber;
            String parseString;
            String permission;
            String syntax;
        }

        @Getter
        @NoArgsConstructor
        public static final class Prompt {
            String message;
            String hard;
            String accept;
            String turn;
            String type;
            String reason;
            String category;
            String id;
            String time;
            String repeatTime;
            String multipleVote;
            String player;
            String number;
            String color;
            String language;
            String url;
            String move;
            String value;
        }

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/anon/")
        Anon anon = new Anon();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ball/")
        Ball ball = new Ball();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ban/")
        Ban ban = new Ban();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/banlist/")
        Banlist banlist = new Banlist();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/broadcast/")
        Broadcast broadcast = new Broadcast();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/chatcolor/")
        Chatcolor chatcolor = new Chatcolor();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/chatsetting/")
        Chatsetting chatsetting = new Chatsetting();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/clearchat/")
        Clearchat clearchat = new Clearchat();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/clearmail/")
        Clearmail clearmail = new Clearmail();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/coin/")
        Coin coin = new Coin();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/deletemessage/")
        Deletemessage deletemessage = new Deletemessage();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/dice/")
        Dice dice = new Dice();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/do/")
        Do Do = new Do();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/flectonepulse/")
        Flectonepulse flectonepulse = new Flectonepulse();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/geolocate/")
        Geolocate geolocate = new Geolocate();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/helper/")
        Helper helper = new Helper();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ignore/")
        Ignore ignore = new Ignore();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ignorelist/")
        Ignorelist ignorelist = new Ignorelist();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/kick/")
        Kick kick = new Kick();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/mail/")
        Mail mail = new Mail();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/maintenance/")
        Maintenance maintenance = new Maintenance();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/me/")
        Me me = new Me();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/mute/")
        Mute mute = new Mute();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/mutelist/")
        Mutelist mutelist = new Mutelist();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/online/")
        Online online = new Online();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ping/")
        Ping ping = new Ping();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/poll/")
        Poll poll = new Poll();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/reply/")
        Reply reply = new Reply();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/rockpaperscissors/")
        Rockpaperscissors rockpaperscissors = new Rockpaperscissors();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/spy/")
        Spy spy = new Spy();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/stream/")
        Stream stream = new Stream();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/symbol/")
        Symbol symbol = new Symbol();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/tell/")
        Tell tell = new Tell();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/tictactoe/")
        Tictactoe tictactoe = new Tictactoe();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/toponline/")
        Toponline toponline = new Toponline();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/translateto/")
        Translateto translateto = new Translateto();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/try/")
        Try Try = new Try();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/unban/")
        Unban unban = new Unban();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/unmute/")
        Unmute unmute = new Unmute();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/unwarn/")
        Unwarn unwarn = new Unwarn();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/warn/")
        Warn warn = new Warn();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/warnlist/")
        Warnlist warnlist = new Warnlist();

        @Getter
        public static final class Anon implements SubCommandConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Ball implements SubCommandConfig, Localizable {
            String format;

            @JsonMerge(OptBoolean.FALSE)
            List<String> answers = new LinkedList<>();
        }

        @Getter
        public static final class Ban implements SubCommandConfig, Localizable {
            String nullPlayer;
            String nullTime;

            @JsonMerge(OptBoolean.FALSE)
            ReasonMap reasons = new ReasonMap();

            String server;
            String person;
            String connectionAttempt;

            @Getter
            @NoArgsConstructor
            public static final class Type {
                String connectionAttempt;

                @JsonMerge(OptBoolean.FALSE)
                ReasonMap reasons = new ReasonMap();
            }
        }

        @Getter
        public static final class Banlist implements SubCommandConfig, Localizable {
            String empty;
            String nullPage;
            String nullPlayer;
            ListTypeMessage global = new ListTypeMessage();
            ListTypeMessage player = new ListTypeMessage();
        }

        @Getter
        public static final class Broadcast implements SubCommandConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Chatcolor implements SubCommandConfig, Localizable {
            String nullPlayer;
            String nullType;
            String nullColor;
            String format;
        }

        @Getter
        public static final class Chatsetting implements SubCommandConfig, Localizable {
            String noPermission;
            String disabledSelf;
            String disabledOther;

            String inventory;
            Checkbox checkbox = new Checkbox();
            Menu menu = new Menu();

            @Getter
            @NoArgsConstructor
            public static final class Checkbox {
                String enabledColor;
                String enabledHover;
                String disabledColor;
                String disabledHover;

                @JsonMerge(OptBoolean.FALSE)
                Map<String, String> types = new LinkedHashMap<>();
            }

            @Getter
            @NoArgsConstructor
            public static final class Menu {

                SubMenu chat = new SubMenu();
                SubMenu see = new SubMenu();
                SubMenu out = new SubMenu();

                @Getter
                @NoArgsConstructor
                public static class SubMenu {
                    String item;
                    String inventory;

                    @JsonMerge(OptBoolean.FALSE)
                    Map<String, String> types = new LinkedHashMap<>();
                }

            }
        }

        @Getter
        public static final class Clearchat implements SubCommandConfig, Localizable {
            String nullPlayer;
            String format;
        }

        @Getter
        public static final class Clearmail implements SubCommandConfig, Localizable {
            String nullMail;
            String format;
        }

        @Getter
        public static final class Coin implements SubCommandConfig, Localizable {
            String head;
            String tail;
            String format;
            String formatDraw;
        }

        @Getter
        public static final class Deletemessage implements SubCommandConfig, Localizable {
            String nullMessage;
            String format;
        }

        @Getter
        public static final class Dice implements SubCommandConfig, Localizable {
            @JsonMerge(OptBoolean.FALSE)
            Map<Integer, String> symbols = new LinkedHashMap<>();

            String format;
        }

        @Getter
        public static final class Do implements SubCommandConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Flectonepulse implements SubCommandConfig, Localizable {
            String nullHostEditor;
            String formatFalse;
            String formatTrue;
            String formatWebStarting;
            String formatEditor;
        }

        @Getter
        public static final class Geolocate implements SubCommandConfig, Localizable {
            String nullPlayer;
            String nullOrError;
            String format;
        }

        @Getter
        public static final class Helper implements SubCommandConfig, Localizable {
            String nullHelper;
            String global;
            String player;
        }

        @Getter
        public static final class Ignore implements SubCommandConfig, Localizable {
            String nullPlayer;
            String myself;
            String he;
            String you;
            String formatTrue;
            String formatFalse;
        }

        @Getter
        public static final class Ignorelist implements SubCommandConfig, Localizable {
            String empty;
            String nullPage;
            String header;
            String line;
            String footer;
        }

        @Getter
        public static final class Kick implements SubCommandConfig, Localizable {
            String nullPlayer;

            @JsonMerge(OptBoolean.FALSE)
            ReasonMap reasons = new ReasonMap();

            String server;
            String person;
        }

        @Getter
        public static final class Mail implements SubCommandConfig, Localizable {
            String nullPlayer;
            String onlinePlayer;
            String sender;
            String receiver;
        }

        @Getter
        public static final class Maintenance implements SubCommandConfig, Localizable {
            String serverDescription;
            String serverVersion;
            String kick;
            String formatTrue;
            String formatFalse;
        }

        @Getter
        public static final class Me implements SubCommandConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Mute implements SubCommandConfig, Localizable {
            String nullPlayer;
            String nullTime;

            @JsonMerge(OptBoolean.FALSE)
            ReasonMap reasons = new ReasonMap();

            String server;
            String person;
        }

        @Getter
        public static final class Mutelist implements SubCommandConfig, Localizable {
            String empty;
            String nullPage;
            String nullPlayer;
            ListTypeMessage global = new ListTypeMessage();
            ListTypeMessage player = new ListTypeMessage();
        }

        @Getter
        public static final class Online implements SubCommandConfig, Localizable {
            String nullPlayer;
            String formatCurrent;
            String formatFirst;
            String formatLast;
            String formatTotal;
        }

        @Getter
        public static final class Ping implements SubCommandConfig, Localizable {
            String nullPlayer;
            String format;
        }

        @Getter
        public static final class Poll implements SubCommandConfig, Localizable {
            String nullPoll;
            String expired;
            String already;
            String voteTrue;
            String voteFalse;
            String format;
            String answerTemplate;
            Status status = new Status();
            Modern modern = new Modern();

            @Getter
            public static final class Status {
                String start;
                String run;
                String end;
            }

            @Getter
            public static final class Modern {
                String header;
                String inputName;
                String inputInitial;
                String multipleName;
                String endTimeName;
                String repeatTimeName;
                String newAnswerButtonName;
                String removeAnswerButtonName;
                String inputAnswerName;
                String inputAnswersInitial;
                String createButtonName;
            }
        }

        @Getter
        public static final class Reply implements SubCommandConfig, Localizable {
            String nullReceiver;
        }

        @Getter
        public static final class Rockpaperscissors implements SubCommandConfig, Localizable {
            String nullPlayer;
            String nullGame;
            String wrongMove;
            String already;
            String myself;
            String sender;
            String receiver;
            String formatMove;
            String formatWin;
            String formatDraw;

            @JsonMerge(OptBoolean.FALSE)
            Map<String, String> strategies = new LinkedHashMap<>();
        }

        @Getter
        public static final class Spy implements SubCommandConfig, Localizable {
            String formatTrue;
            String formatFalse;
            String formatLog;
        }

        @Getter
        public static final class Stream implements SubCommandConfig, Localizable {
            String already;
            String not;
            String prefixTrue;
            String prefixFalse;
            String urlTemplate;
            String formatStart;
            String formatEnd;
        }

        @Getter
        public static final class Symbol implements SubCommandConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Tell implements SubCommandConfig, Localizable {
            String nullPlayer;
            String sender;
            String receiver;
            String myself;
        }

        @Getter
        public static final class Tictactoe implements SubCommandConfig, Localizable {
            String nullPlayer;
            String myself;
            String wrongGame;
            String wrongMove;
            String wrongByPlayer;
            Symbol symbol = new Symbol();

            @Getter
            @NoArgsConstructor
            public static final class Symbol {
                String blank;
                String first;
                String firstRemove;
                String firstWin;
                String second;
                String secondRemove;
                String secondWin;
            }

            String field;
            String currentMove;
            String lastMove;
            String formatMove;
            String formatWin;
            String formatDraw;
            String sender;
            String receiver;
        }

        @Getter
        public static final class Toponline implements SubCommandConfig, Localizable {
            String nullPage;
            String header;
            String line;
            String footer;
        }

        @Getter
        public static final class Translateto implements SubCommandConfig, Localizable {
            String nullOrError;
            String format;
        }

        @Getter
        public static final class Try implements SubCommandConfig, Localizable {
            String formatTrue;
            String formatFalse;
        }

        @Getter
        public static final class Unban implements SubCommandConfig, Localizable {
            String nullPlayer;
            String notBanned;
            String format;
        }

        @Getter
        public static final class Unmute implements SubCommandConfig, Localizable {
            String nullPlayer;
            String notMuted;
            String format;
        }

        @Getter
        public static final class Unwarn implements SubCommandConfig, Localizable {
            String nullPlayer;
            String notWarned;
            String format;
        }

        @Getter
        public static final class Warn implements SubCommandConfig, Localizable {
            String nullPlayer;
            String nullTime;

            @JsonMerge(OptBoolean.FALSE)
            ReasonMap reasons = new ReasonMap();

            String server;
            String person;
        }

        @Getter
        public static final class Warnlist implements SubCommandConfig, Localizable {
            String empty;
            String nullPage;
            String nullPlayer;
            ListTypeMessage global = new ListTypeMessage();
            ListTypeMessage player = new ListTypeMessage();
        }
    }

    @Getter
    public static final class Integration implements IntegrationConfig, Localizable {

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/discord/")
        Discord discord = new Discord();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/telegram/")
        Telegram telegram = new Telegram();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/twitch/")
        Twitch twitch = new Twitch();

        @Override
        public SubIntegrationConfig getAdvancedban() {
            return null;
        }

        @Override
        public SubIntegrationConfig getDeepl() {
            return null;
        }

        @Override
        public SubIntegrationConfig getInteractivechat() {
            return null;
        }

        @Override
        public SubIntegrationConfig getItemsadder() {
            return null;
        }

        @Override
        public SubIntegrationConfig getLitebans() {
            return null;
        }

        @Override
        public SubIntegrationConfig getLuckperms() {
            return null;
        }

        @Override
        public SubIntegrationConfig getMaintenance() {
            return null;
        }

        @Override
        public SubIntegrationConfig getMinimotd() {
            return null;
        }

        @Override
        public SubIntegrationConfig getMiniplaceholders() {
            return null;
        }

        @Override
        public SubIntegrationConfig getMotd() {
            return null;
        }

        @Override
        public SubIntegrationConfig getPlaceholderapi() {
            return null;
        }

        @Override
        public SubIntegrationConfig getPlasmovoice() {
            return null;
        }

        @Override
        public SubIntegrationConfig getSimplevoice() {
            return null;
        }

        @Override
        public SubIntegrationConfig getSkinsrestorer() {
            return null;
        }

        @Override
        public SubIntegrationConfig getSupervanish() {
            return null;
        }

        @Override
        public SubIntegrationConfig getVault() {
            return null;
        }

        @Override
        public SubIntegrationConfig getYandex() {
            return null;
        }

        @Getter
        public static final class Discord implements SubIntegrationConfig, Localizable {
            String forMinecraft;

            @JsonMerge(OptBoolean.FALSE)
            Map<String, String> infoChannel = new LinkedHashMap<>();

            @JsonMerge(OptBoolean.FALSE)
            Map<String, ChannelEmbed> messageChannel = new LinkedHashMap<>();

            @Getter
            @NoArgsConstructor
            public final static class ChannelEmbed {
                String content;
                Webhook webhook = new Webhook();
                Embed embed = new Embed();
            }

            @Getter
            @NoArgsConstructor
            public static final class Webhook {
                boolean enable;
                String avatar;
                String content;
            }

            @Getter
            @NoArgsConstructor
            public static final class Embed {
                boolean enable;
                String color;
                String title;
                String url;
                Author author = new Author();
                String description;
                String thumbnail;

                @JsonMerge(OptBoolean.FALSE)
                List<Field> fields = new LinkedList<>();

                String image;
                boolean timestamp;
                Footer footer = new Footer();

                @Getter
                @NoArgsConstructor
                public static final class Author {
                    String name;
                    String url;
                    String iconUrl;
                }

                @Getter
                @NoArgsConstructor
                public static final class Footer {
                    String text;
                    String iconUrl;
                }

                @Getter
                @NoArgsConstructor
                @AllArgsConstructor
                public static final class Field {
                    String name;
                    String value;
                    boolean inline;
                }
            }
        }

        @Getter
        public static final class Telegram implements SubIntegrationConfig, Localizable {
            String forMinecraft;

            @JsonMerge(OptBoolean.FALSE)
            Map<String, String> messageChannel = new LinkedHashMap<>();
        }

        @Override
        public SubIntegrationConfig getTriton() {
            return null;
        }

        @Getter
        public static final class Twitch implements SubIntegrationConfig, Localizable {
            String forMinecraft;

            @JsonMerge(OptBoolean.FALSE)
            Map<String, String> messageChannel = new LinkedHashMap<>();
        }
    }

    @Getter
    @Setter
    public static final class Message implements MessageConfig, Localizable {

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/advancement/")
        Advancement advancement = new Advancement();

        @Override
        public SubMessageConfig getAnvil() {
            return null;
        }

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/afk/")
        Afk afk = new Afk();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/attribute/")
        Attribute attribute = new Attribute();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/auto/")
        Auto auto = new Auto();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/bed/")
        Bed bed = new Bed();

        @Override
        public SubMessageConfig getBook() {
            return null;
        }

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/brand/")
        Brand brand = new Brand();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/bubble/")
        Bubble bubble = new Bubble();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/chat/")
        Chat chat = new Chat();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/clear/")
        Clear clear = new Clear();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/clone/")
        Clone clone = new Clone();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/commandblock/")
        Commandblock commandblock = new Commandblock();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/damage/")
        Damage damage = new Damage();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/death/")
        Death death = new Death();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/debugstick/")
        Debugstick debugstick = new Debugstick();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/deop/")
        Deop deop = new Deop();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/dialog/")
        Dialog dialog = new Dialog();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/difficulty/")
        Difficulty difficulty = new Difficulty();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/effect/")
        Effect effect = new Effect();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/enchant/")
        Enchant enchant = new Enchant();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/execute/")
        Execute execute = new Execute();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/experience/")
        Experience experience = new Experience();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/fill/")
        Fill fill = new Fill();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/fillbiome/")
        Fillbiome fillbiome = new Fillbiome();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/")
        Format format = new Format();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/gamemode/")
        Gamemode gamemode = new Gamemode();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/gamerule/")
        Gamerule gamerule = new Gamerule();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/give/")
        Give give = new Give();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/greeting/")
        Greeting greeting = new Greeting();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/join/")
        Join join = new Join();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/kill/")
        Kill kill = new Kill();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/locate/")
        Locate locate = new Locate();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/objective/")
        Objective objective = new Objective();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/op/")
        Op op = new Op();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/particle/")
        Particle particle = new Particle();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/quit/")
        Quit quit = new Quit();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/recipe/")
        Recipe recipe = new Recipe();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/reload/")
        Reload reload = new Reload();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/ride/")
        Ride ride = new Ride();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/rightclick/")
        Rightclick rightclick = new Rightclick();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/rotate/")
        Rotate rotate = new Rotate();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/save/")
        Save save = new Save();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/seed/")
        Seed seed = new Seed();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/setblock/")
        Setblock setblock = new Setblock();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/sidebar/")
        Sidebar sidebar = new Sidebar();

        @Override
        public SubMessageConfig getSign() {
            return null;
        }

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/sleep/")
        Sleep sleep = new Sleep();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/sound/")
        Sound sound = new Sound();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/spawn/")
        Spawn spawn = new Spawn();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/")
        Status status = new Status();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/stop/")
        Stop stop = new Stop();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/summon/")
        Summon summon = new Summon();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/")
        Tab tab = new Tab();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/teleport/")
        Teleport teleport = new Teleport();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/time/")
        Time time = new Time();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/update/")
        Update update = new Update();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/weather/")
        Weather weather = new Weather();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/worldborder/")
        Worldborder worldborder = new Worldborder();

        @Getter
        public static final class Advancement implements SubMessageConfig, Localizable {

            String formatTask;
            String formatGoal;
            String formatChallenge;
            String formatTaken;
            Tag tag = new Tag();
            Command revoke = new Command();
            Command grant = new Command();

            @Getter
            @NoArgsConstructor
            public static final class Tag {
                String task;
                String challenge;
            }

            @Getter
            @NoArgsConstructor
            public static final class Command {
                String manyToOne;
                String oneToMany;
                String manyToMany;
                String oneToOne;
                String criterionToMany;
                String criterionToOne;
            }
        }

        @Getter
        public static final class Afk implements SubMessageConfig, Localizable {
            String suffix;
            Format formatTrue = new Format();
            Format formatFalse = new Format();

            @Getter
            @NoArgsConstructor
            public static final class Format {
                String global;
                String local;
            }
        }

        @Getter
        public static final class Attribute implements SubMessageConfig, Localizable {
            BaseValue baseValue = new BaseValue();
            Modifier modifier = new Modifier();
            String valueGet;

            @Getter
            @NoArgsConstructor
            public static final class BaseValue {
                String get;
                String reset;
                String set;
            }

            @Getter
            @AllArgsConstructor
            @NoArgsConstructor
            public static final class Modifier {
                String add;
                String remove;
                String valueGet;
            }
        }

        @Getter
        public static final class Auto implements SubMessageConfig, Localizable {
            @JsonMerge(OptBoolean.FALSE)
            Map<String, List<String>> types = new LinkedHashMap<>();
        }

        @Getter
        public static final class Bed implements SubMessageConfig, Localizable {
            String noSleep;
            String notSafe;
            String obstructed;
            String occupied;
            String tooFarAway;
        }

        @Getter
        public static final class Brand implements SubMessageConfig, Localizable {
            @JsonMerge(OptBoolean.FALSE)
            List<String> values = new LinkedList<>();
        }

        @Getter
        public static final class Bubble implements SubMessageConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Chat implements SubMessageConfig, Localizable {
            String nullChat;
            String nullReceiver;

            @JsonMerge(OptBoolean.FALSE)
            Map<String, String> types = new LinkedHashMap<>();
        }

        @Getter
        public static final class Clear implements SubMessageConfig, Localizable {
            String single;
            String multiple;
        }

        @Getter
        public static final class Clone implements SubMessageConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Commandblock implements SubMessageConfig, Localizable {
            String notEnabled;
            String format;
        }

        @Getter
        public static final class Damage implements SubMessageConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Death implements SubMessageConfig, Localizable {
            @JsonMerge(OptBoolean.FALSE)
            Map<String, String> types = new LinkedHashMap<>();
        }

        @Getter
        public static final class Debugstick implements SubMessageConfig, Localizable {
            String empty;
            String select;
            String update;
        }

        @Getter
        public static final class Deop implements SubMessageConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Dialog implements SubMessageConfig, Localizable {
            SingleMultiple clear = new SingleMultiple();
            SingleMultiple show = new SingleMultiple();
        }

        @Getter
        public static final class Difficulty implements SubMessageConfig, Localizable {
            String query;
            String success;
        }

        @Getter
        public static final class Effect implements SubMessageConfig, Localizable {

            Clear clear = new Clear();
            SingleMultiple give = new SingleMultiple();

            @Getter
            public static final class Clear {
                SingleMultiple everything = new SingleMultiple();
                SingleMultiple specific = new SingleMultiple();
            }

        }

        @Getter
        public static final class Enchant implements SubMessageConfig, Localizable {
            String single;
            String multiple;
        }

        @Getter
        public static final class Execute implements SubMessageConfig, Localizable {
            String pass;
            String passCount;
        }

        @Getter
        public static final class Experience implements SubMessageConfig, Localizable {

            AddSet add = new AddSet();
            Query query = new Query();
            AddSet set = new AddSet();

            @Getter
            public static final class Query {
                String levels;
                String points;
            }


            @Getter
            @NoArgsConstructor
            public static final class AddSet {
                SingleMultiple levels = new SingleMultiple();
                SingleMultiple points = new SingleMultiple();
            }

        }

        @Getter
        public static final class Fill implements SubMessageConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Fillbiome implements SubMessageConfig, Localizable {
            String format;
            String formatCount;
        }

        @Getter
        public static final class Format implements FormatMessageConfig, Localizable {

            @Override
            public SubFormatMessageConfig getFcolor() {
                return null;
            }

            @Override
            public SubFormatMessageConfig getFixation() {
                return null;
            }

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/replacement/")
            Replacement replacement = new Replacement();

            @Override
            public SubFormatMessageConfig getScoreboard() {
                return null;
            }

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/mention/")
            Mention mention = new Mention();

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/")
            Moderation moderation = new Moderation();

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/name_/")
            Name name_ = new Name();

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/questionanswer/")
            QuestionAnswer questionAnswer = new QuestionAnswer();

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/translate/")
            Translate translate = new Translate();

            @Override
            public SubFormatMessageConfig getWorld() {
                return null;
            }

            @Getter
            public static final class Replacement implements SubFormatMessageConfig, Localizable {
                String spoilerSymbol;

                @JsonMerge(OptBoolean.FALSE)
                Map<String, String> values;
            }

            @Getter
            public static final class Mention implements SubFormatMessageConfig, Localizable {
                String person;
                String format;
            }

            @Getter
            public static final class Moderation implements ModerationFormatMessageConfig, Localizable {

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/delete/")
                Delete delete = new Delete();

                @Override
                public SubModerationFormatMessageConfig getCaps() {
                    return null;
                }

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/newbie/")
                Newbie newbie = new Newbie();

                @Override
                public SubModerationFormatMessageConfig getFlood() {
                    return null;
                }

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/swear/")
                Swear swear = new Swear();

                @Getter
                public static final class Delete implements SubModerationFormatMessageConfig, Localizable {
                    String placeholder;
                    String format;
                }

                @Getter
                public static final class Newbie implements SubModerationFormatMessageConfig, Localizable {
                    String reason;
                }

                @Getter
                public static final class Swear implements SubModerationFormatMessageConfig, Localizable {
                    String symbol;
                }

            }

            @Getter
            @Setter
            public static final class Name implements SubFormatMessageConfig, Localizable {
                String constant;
                String display;
                String entity;
                String unknown;
                String invisible;
            }

            @Getter
            public static final class QuestionAnswer implements SubFormatMessageConfig, Localizable {
                @JsonMerge(OptBoolean.FALSE)
                Map<String, String> questions = new LinkedHashMap<>();
            }

            @Getter
            public static final class Translate implements SubFormatMessageConfig, Localizable {
                String action;
            }
        }

        @Getter
        public static final class Gamemode implements SubMessageConfig, Localizable {
            String setDefault;
            String self;
            String other;
        }

        @Getter
        public static final class Gamerule implements SubMessageConfig, Localizable {
            String formatQuery;
            String formatSet;
        }

        @Getter
        public static final class Give implements SubMessageConfig, Localizable {
            String single;
            String multiple;
        }

        @Getter
        public static final class Greeting implements SubMessageConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Join implements SubMessageConfig, Localizable {
            String format;
            String formatFirstTime;
        }

        @Getter
        public static final class Kill implements SubMessageConfig, Localizable {
            String single;
            String multiple;
        }

        @Getter
        public static final class Locate implements SubMessageConfig, Localizable {
            String biome;
            String poi;
            String structure;
        }

        @Getter
        public static final class Objective implements ObjectiveMessageConfig, Localizable {


            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/objective/belowname/")
            Belowname belowname = new Belowname();

            @Override
            public SubObjectiveMessageConfig getTabname() {
                return null;
            }

            @Getter
            public static final class Belowname implements SubObjectiveMessageConfig, Localizable {
                String format;
            }

        }

        @Getter
        public static final class Op implements SubMessageConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Particle implements SubMessageConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Quit implements SubMessageConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Recipe implements SubMessageConfig, Localizable {
            SingleMultiple give = new SingleMultiple();
            SingleMultiple take = new SingleMultiple();
        }

        @Getter
        public static final class Reload implements SubMessageConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Ride implements SubMessageConfig, Localizable {
            String dismount;
            String mount;
        }

        @Getter
        public static final class Rightclick implements SubMessageConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Rotate implements SubMessageConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Save implements SubMessageConfig, Localizable {
            String disabled;
            String enabled;
            String saving;
            String success;
        }

        @Getter
        public static final class Seed implements SubMessageConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Setblock implements SubMessageConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Sidebar implements SubMessageConfig, Localizable {
            @JsonMerge(OptBoolean.FALSE)
            List<List<String>> values = new LinkedList<>();
        }

        @Getter
        public static final class Sleep implements SubMessageConfig, Localizable {
            String notPossible;
            String playersSleeping;
            String skippingNight;
        }

        @Getter
        public static final class Sound implements SubMessageConfig, Localizable {
            Play play = new Play();
            Stop stop = new Stop();

            @Getter
            @NoArgsConstructor
            public static final class Play {
                String multiple;
                String single;
            }

            @Getter
            @NoArgsConstructor
            public static final class Stop {
                String sourceAny;
                String sourceSound;
                String sourcelessAny;
                String sourcelessSound;
            }
        }

        @Getter
        public static final class Spawn implements SubMessageConfig, Localizable {
            String notValid;
            String set;
            String setWorld;
            String single;
            String multiple;
        }

        @Getter
        public static final class Status implements StatusMessageConfig, Localizable {

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/motd/")
            MOTD motd = new MOTD();

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/players/")
            Players players = new Players();

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/version/")
            Version version = new Version();

            @Override
            public SubStatusMessageConfig getIcon() {
                return null;
            }

            @Getter
            public static final class MOTD implements SubStatusMessageConfig, Localizable {
                @JsonMerge(OptBoolean.FALSE)
                List<String> values = new LinkedList<>();
            }

            @Getter
            public static final class Players implements SubStatusMessageConfig, Localizable {
                @JsonMerge(OptBoolean.FALSE)
                List<Sample> samples = new LinkedList<>();
                String full;

                @Getter
                @NoArgsConstructor
                public static final class Sample {
                    String name = "<players>";
                    String id = null;
                }
            }

            @Getter
            public static final class Version implements SubStatusMessageConfig, Localizable {
                String name;
            }

        }

        @Getter
        public static final class Stop implements SubMessageConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Summon implements SubMessageConfig, Localizable {
            String format;
        }

        @Getter
        public static final class Tab implements TabMessageConfig, Localizable {

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/header/")
            Header header = new Header();

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/footer/")
            Footer footer = new Footer();

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/playerlistname/")
            Playerlistname playerlistname = new Playerlistname();

            @Getter
            public static final class Footer implements SubTabMessageConfig, Localizable {
                @JsonMerge(OptBoolean.FALSE)
                List<List<String>> lists = new LinkedList<>();
            }

            @Getter
            public static final class Header implements SubTabMessageConfig, Localizable {
                @JsonMerge(OptBoolean.FALSE)
                List<List<String>> lists = new LinkedList<>();
            }

            @Getter
            public static final class Playerlistname implements SubTabMessageConfig, Localizable {
                String format;
            }
        }

        @Getter
        public static final class Teleport implements SubMessageConfig, Localizable {
            SingleMultiple entity = new SingleMultiple();
            SingleMultiple location = new SingleMultiple();
        }

        @Getter
        public static final class Time implements SubMessageConfig, Localizable {
            String query;
            String set;
        }

        @Getter
        public static final class Update implements SubMessageConfig, Localizable {
            String formatPlayer;
            String formatConsole;
        }

        @Getter
        public static final class Weather implements SubMessageConfig, Localizable {
            String formatClear;
            String formatRain;
            String formatThunder;
        }

        @Getter
        public static final class Worldborder implements SubMessageConfig, Localizable {
            String center;
            Damage damage = new Damage();
            String get;
            Set set = new Set();
            Warning warning = new Warning();

            @Getter
            @NoArgsConstructor
            public static final class Damage {
                String amount;
                String buffer;
            }

            @Getter
            @NoArgsConstructor
            public static final class Set {
                String grow;
                String immediate;
                String shrink;
            }

            @Getter
            @NoArgsConstructor
            public static final class Warning {
                String distance;
                String time;
            }
        }

    }

    public interface Localizable {}

    @Getter
    @NoArgsConstructor
    public static final class ListTypeMessage {
        String header;
        String line;
        String footer;
    }

    public static class ReasonMap extends LinkedHashMap<String, String> {

        public String getConstant(String reason) {
            if (StringUtils.isEmpty(reason)) {
                return super.getOrDefault("default", "UNKNOWN");
            }

            return super.getOrDefault(reason, reason);
        }

    }

    @Getter
    @NoArgsConstructor
    public static final class SingleMultiple {
        String single;
        String multiple;
    }

}
