package net.flectone.pulse.config;

import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.util.*;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.util.constant.AdventureTag;

import java.nio.file.Path;
import java.util.*;

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
public final class Message extends FileSerializable implements ModuleConfig.MessageConfig {

    public Message(Path projectPath) {
        super(projectPath.resolve("message.yml"));
    }

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/")})
    private boolean enable = true;

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/advancement/")})
    private Advancement advancement = new Advancement();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/afk/")})
    private Afk afk = new Afk();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/anvil/")})
    private Anvil anvil = new Anvil();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/attribute/")})
    private Attribute attribute = new Attribute();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/auto/")})
    private Auto auto = new Auto();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/bed/")})
    private Bed bed = new Bed();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/book/")})
    private Book book = new Book();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/brand/")})
    private Brand brand = new Brand();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/bubble/")})
    private Bubble bubble = new Bubble();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/chat/")})
    private Chat chat = new Chat();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/clear/")})
    private Clear clear = new Clear();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/clone/")})
    private Clone clone = new Clone();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/commandblock/")})
    private Commandblock commandblock = new Commandblock();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/damage/")})
    private Damage damage = new Damage();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/death/")})
    private Death death = new Death();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/debugstick/")})
    private Debugstick debugstick = new Debugstick();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/deop/")})
    private Deop deop = new Deop();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/dialog/")})
    private Dialog dialog = new Dialog();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/difficulty/")})
    private Difficulty difficulty = new Difficulty();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/effect/")})
    private Effect effect = new Effect();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/enchant/")})
    private Enchant enchant = new Enchant();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/execute/")})
    private Execute execute = new Execute();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/experience/")})
    private Experience experience = new Experience();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/fill/")})
    private Fill fill = new Fill();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/fillbiome/")})
    private Fillbiome fillbiome = new Fillbiome();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/")})
    private Format format = new Format();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/gamemode/")})
    private Gamemode gamemode = new Gamemode();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/gamerule/")})
    private Gamerule gamerule = new Gamerule();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/give/")})
    private Give give = new Give();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/greeting/")})
    private Greeting greeting = new Greeting();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/join/")})
    private Join join = new Join();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/kill/")})
    private Kill kill = new Kill();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/locate/")})
    private Locate locate = new Locate();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/objective/")})
    private Objective objective = new Objective();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/op/")})
    private Op op = new Op();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/particle/")})
    private Particle particle = new Particle();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/quit/")})
    private Quit quit = new Quit();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/reload/")})
    private Reload reload = new Reload();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/ride/")})
    private Ride ride = new Ride();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/rightclick/")})
    private Rightclick rightclick = new Rightclick();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/rotate/")})
    private Rotate rotate = new Rotate();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/save/")})
    private Save save = new Save();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/seed/")})
    private Seed seed = new Seed();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/setblock/")})
    private Setblock setblock = new Setblock();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/sidebar/")})
    private Sidebar sidebar = new Sidebar();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/sign/")})
    private Sign sign = new Sign();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/sleep/")})
    private Sleep sleep = new Sleep();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/spawn/")})
    private Spawn spawn = new Spawn();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/status/")})
    private Status status = new Status();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/stop/")})
    private Stop stop = new Stop();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/summon/")})
    private Summon summon = new Summon();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/tab/")})
    private Tab tab = new Tab();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/teleport/")})
    private Teleport teleport = new Teleport();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/time/")})
    private Time time = new Time();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/update/")})
    private Update update = new Update();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/weather/")})
    private Weather weather = new Weather();

    @Getter
    public static final class Advancement implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private boolean grant = true;
        private boolean revoke = true;
        private Range range = Range.get(Range.Type.SERVER);
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Afk implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Range range = Range.get(Range.Type.SERVER);
        private int delay = 3000;
        private List<String> ignore = new ArrayList<>(List.of("afk"));
        private Destination destination = new Destination();
        private Ticker ticker = new Ticker(true, 20);
    }

    @Getter
    public static final class Anvil implements SubMessageConfig, Config.IEnable {
        private boolean enable = false;
    }

    @Getter
    public static final class Attribute implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Auto implements SubMessageConfig, Config.IEnable {
        private boolean enable = false;
        private Map<String, Type> types = new LinkedHashMap<>(){
            {
                put("announcement", new Type());
            }
        };

        @Getter
        @NoArgsConstructor
        public static final class Type {
            private boolean random = true;
            private Destination destination = new Destination();
            private Ticker ticker = new Ticker(true, 9000);
            private Sound sound = new Sound();
        }
    }

    @Getter
    public static final class Bed implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination(Destination.Type.ACTION_BAR);
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Book implements SubMessageConfig, Config.IEnable {
        private boolean enable = false;
    }

    @Getter
    public static final class Brand implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private boolean random = true;
        private Destination destination = new Destination(Destination.Type.BRAND);
        private Ticker ticker = new Ticker(true, 100);
    }

    @Getter
    public static final class Bubble implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private int maxCount = 3;
        private int maxLength = 30;
        private int elevation = 1;
        private double distance = 30.0;
        private double readSpeed = 90.0;
        private double handicapChars = 10.0;
        private String wordBreakHint = "‑";

        private Interaction interaction = new Interaction();
        private Modern modern = new Modern();

        @Getter
        @NoArgsConstructor
        public static final class Interaction {
            private boolean enable = true;
            private float height = 0.4f;
        }

        @Getter
        @NoArgsConstructor
        public static final class Modern {
            private boolean enable = true;
            private boolean hasShadow = false;
            private int animationTime = 5;
            private float scale = 1.0f;
            private String background = "#00000040";
            private BubbleModule.Billboard billboard = BubbleModule.Billboard.CENTER;
        }

    }

    @Getter
    public static final class Chat implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Mode mode = Mode.BUKKIT;
        private Event.Priority priority = Event.Priority.NORMAL;
        private Map<String, Type> types = new LinkedHashMap<>(){
            {
                put("local", new Type("", true, true, Range.get(100), 0));
                put("global", new Type("!", true, false, Range.get(Range.Type.PROXY), 5));
            }
        };

        public enum Mode {
            BUKKIT,
            PAPER,
            PACKET
        }

        @Getter
        @NoArgsConstructor
        public static final class Type {
            private boolean enable = true;
            private boolean cancel = false;
            private Range range = Range.get(0);
            private int priority = 0;
            private String trigger = "";
            private NullReceiver nullReceiver = new NullReceiver();
            private Destination destination = new Destination();
            private Cooldown cooldown = new Cooldown();
            private Sound sound = new Sound();

            public Type(String trigger, boolean nullReceiver, boolean cancel, Range range, int priority) {
                this.trigger = trigger;
                this.cancel = cancel;
                this.nullReceiver = new NullReceiver(nullReceiver);
                this.range = range;
                this.priority = priority;
            }

            @Getter
            @NoArgsConstructor
            public static final class NullReceiver {
                private boolean enable = true;
                private Destination destination = new Destination(Destination.Type.ACTION_BAR, new Times(0, 20, 0));

                public NullReceiver(boolean enable) {
                    this.enable = enable;
                }
            }
        }
    }

    @Getter
    public static final class Clear implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Sound sound = new Sound();
        private Destination destination = new Destination();
    }

    @Getter
    public static final class Clone implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Sound sound = new Sound();
        private Destination destination = new Destination();
    }

    @Getter
    public static final class Commandblock implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Sound sound = new Sound();
        private Destination destination = new Destination();
    }

    @Getter
    public static final class Damage implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Sound sound = new Sound();
        private Destination destination = new Destination();
    }

    @Getter
    public static final class Death implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Range range = Range.get(Range.Type.SERVER);
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Debugstick implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination(Destination.Type.ACTION_BAR, new Times(0, 20, 0));
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Deop implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Dialog implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Difficulty implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Effect implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Enchant implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Execute implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Experience implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Fill implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Fillbiome implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Format implements FormatMessageConfig, Config.IEnable {

        private boolean enable = true;
        private boolean convertLegacyColor = true;

        private List<AdventureTag> adventureTags = new ArrayList<>(List.of(AdventureTag.values()));

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/fcolor/")})
        private FColor fcolor = new FColor();

        @Deprecated(forRemoval = true)
        private Emoji emoji = new Emoji();

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/fixation/")})
        private Fixation fixation = new Fixation();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/mention/")})
        private Mention mention = new Mention();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/moderation/")})
        private Moderation moderation = new Moderation();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/name_/")})
        private Name name_ = new Name();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/questionanswer/")})
        private QuestionAnswer questionAnswer = new QuestionAnswer();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/replacement/")})
        private Replacement replacement = new Replacement();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/scoreboard/")})
        private Scoreboard scoreboard = new Scoreboard();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/translate/")})
        private Translate translate = new Translate();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/world/")})
        private World world = new World();

        @Getter
        @NoArgsConstructor
        public static class Tag {
            private boolean enable = true;
            private String trigger = "";

            public Tag(String trigger) {
                this.trigger = trigger;
            }
        }

        @Getter
        public static final class FColor implements SubFormatMessageConfig, Config.IEnable {
            private boolean enable = true;
            private Map<Integer, String> defaultColors = new LinkedHashMap<>(){
                {
                    put(1, "<gradient:#A6D8FF:#8CC8FF>");
                    put(2, "<gradient:#6BB6FF:#4DA6FF>");
                    put(3, "#A9A9A9");
                    put(4, "#FFFAFA");
                }
            };
        }

        @Getter
        public static final class Emoji implements SubFormatMessageConfig, Config.IEnable {
            private boolean enable = false;
            private Map<String, String> values = new LinkedHashMap<>();
        }

        @Getter
        public static final class Fixation implements SubFormatMessageConfig, Config.IEnable {
            private boolean enable = false;
            private boolean endDot = false;
            private boolean firstLetterUppercase = false;
            private List<String> nonDotSymbols = new LinkedList<>() {
                {
                    push(".");
                    push("!");
                    push("?");
                    push(",");
                    push("\"");
                    push("'");
                    push(":");
                    push(";");
                }
            };
        }

        @Getter
        public static final class Mention implements SubFormatMessageConfig, Config.IEnable {
            private boolean enable = true;
            private String trigger = "@";
            private Destination destination = new Destination(Destination.Type.TOAST, new Toast("minecraft:bell", Toast.Type.TASK));
            private Sound sound = new Sound(true, 0.1f, 0.1f, SoundCategory.NEUTRAL.name(), Sounds.ENTITY_EXPERIENCE_ORB_PICKUP.getName().toString());
        }

        @Getter
        public static final class Moderation implements ModerationFormatMessageConfig, Config.IEnable {

            private boolean enable = true;

            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/moderation/caps/")})
            private Caps caps = new Caps();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/moderation/delete/")})
            private Delete delete = new Delete();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/moderation/newbie/")})
            private Newbie newbie = new Newbie();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/moderation/flood/")})
            private Flood flood = new Flood();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/moderation/swear/")})
            private Swear swear = new Swear();

            @Getter
            public static final class Caps implements SubModerationFormatMessageConfig, Config.IEnable {
                private boolean enable = false;
                private double trigger = 0.7;
            }

            @Getter
            public static final class Delete implements SubModerationFormatMessageConfig, Config.IEnable {
                private boolean enable = false;
                private int historyLength = 100;
            }

            @Getter
            public static final class Newbie implements SubModerationFormatMessageConfig, Config.IEnable {
                private boolean enable = false;
                private Mode mode = Mode.PLAYED_TIME;
                private long timeout = 3600;

                public enum Mode {
                    PLAYED_TIME,
                    SINCE_JOIN,
                }
            }

            @Getter
            public static final class Flood implements SubModerationFormatMessageConfig, Config.IEnable {
                private boolean enable = false;
                private boolean trimToSingle = false;
                private int maxRepeatedSymbols = 10;
                private int maxRepeatedWords = 2;
            }

            @Getter
            public static final class Swear implements SubModerationFormatMessageConfig, Config.IEnable {
                private boolean enable = false;

                private List<String> ignore = new ArrayList<>(List.of("тебя", "тебе"));
                private List<String> trigger = new ArrayList<>(List.of(
                        "((у|[нз]а|(хитро|не)?вз?[ыьъ]|с[ьъ]|(и|ра)[зс]ъ?|(о[тб]|под)[ьъ]?|(.\\B)+?[оаеи])?-?([её]б(?!о[рй])|и[пб][ае][тц]).*?|(н[иеа]|([дп]|верт)о|ра[зс]|з?а|с(ме)?|о(т|дно)?|апч)?-?ху([яйиеёю]|ли(?!ган)).*?|(в[зы]|(три|два|четыре)жды|(н|сук)а)?-?бл(я(?!(х|ш[кн]|мб)[ауеыио]).*?|[еэ][дт]ь?)|(ра[сз]|[зн]а|[со]|вы?|п(ере|р[оие]|од)|и[зс]ъ?|[ао]т)?п[иеё]зд.*?|(за)?п[ие]д[аое]?р([оа]м|(ас)?(ну.*?|и(ли)?[нщктл]ь?)?|(о(ч[еи])?|ас)?к(ой)|юг)[ауеы]?|манд([ауеыи](л(и[сзщ])?[ауеиы])?|ой|[ао]вошь?(е?к[ауе])?|юк(ов|[ауи])?)|муд([яаио].*?|е?н([ьюия]|ей))|мля([тд]ь)?|лять|([нз]а|по|пи)х|м[ао]л[ао]фь([яию]|[еёо]й))(?=[\\s,.:;\"']|$)",
                        "(([уyu]|[нзnz3][аa]|(хитро|не)?[вvwb][зz3]?[ыьъi]|[сsc][ьъ']|(и|[рpr][аa4])[зсzs]ъ?|([оo0][тбtb6]|[пp][оo0][дd9])[ьъ']?|(.\\B)+?[оаеиeo])?-?([еёe][бb6](?!о[рй])|и[пб][ае][тц]).*?|([нn][иеаaie]|([дпdp]|[вv][еe3][рpr][тt])[оo0]|[рpr][аa][зсzc3]|[з3z]?[аa]|с(ме)?|[оo0]([тt]|дно)?|апч)?-?[хxh][уuy]([яйиеёюuie]|ли(?!ган)).*?|([вvw][зы3z]|(три|два|четыре)жды|(н|[сc][уuy][кk])[аa])?-?[бb6][лl]([яy](?!(х|ш[кн]|мб)[ауеыио]).*?|[еэe][дтdt][ь']?)|([рp][аa][сзc3z]|[знzn][аa]|[соsc]|[вv][ыi]?|[пp]([еe][рpr][еe]|[рrp][оиioеe]|[оo0][дd])|и[зс]ъ?|[аоao][тt])?[пpn][иеёieu][зz3][дd9].*?|([зz3][аa])?[пp][иеieu][дd][аоеaoe]?[рrp](ну.*?|[оаoa][мm]|([аa][сcs])?([иiu]([лl][иiu])?[нщктлtlsn]ь?)?|([оo](ч[еиei])?|[аa][сcs])?[кk]([оo]й)?|[юu][гg])[ауеыauyei]?|[мm][аa][нnh][дd]([ауеыayueiи]([лl]([иi][сзc3щ])?[ауеыauyei])?|[оo][йi]|[аоao][вvwb][оo](ш|sh)[ь']?([e]?[кk][ауеayue])?|юк(ов|[ауи])?)|[мm][уuy][дd6]([яyаиоaiuo0].*?|[еe]?[нhn]([ьюия'uiya]|ей))|мля([тд]ь)?|лять|([нз]а|по|пи)х|м[ао]л[ао]фь([яию]|[её]й))(?=[\\s,.:;\"']|$)")
                );
            }

        }

        @Getter
        public static final class Name implements SubFormatMessageConfig, Config.IEnable {
            private boolean enable = true;
            private boolean shouldCheckInvisibility = false;
        }

        @Getter
        public static final class QuestionAnswer implements SubFormatMessageConfig, Config.IEnable {
            private boolean enable = false;
            private Map<String, Question> questions = new LinkedHashMap<>(){
                {
                    put("server", new Question("(?i)\\b(what\\s+is\\s+this\\s+server|what\\'?s\\s+this\\s+server|what\\s+server\\s+is\\s+this)\\b"));
                    put("flectone", new Question("(?i)\\b(flectone|flectonepulse|flecton)\\b"));
                }
            };

            @Getter
            @NoArgsConstructor
            public static final class Question {
                private Range range = Range.get(Range.Type.PLAYER);
                private Destination destination = new Destination(Destination.Type.CHAT);
                private Cooldown cooldown = new Cooldown();
                private Sound sound = new Sound();
                private String target = "";

                public Question(String target) {
                    this.target = target;
                }
            }
        }

        @Getter
        public static final class Replacement implements SubFormatMessageConfig, Config.IEnable {
            private boolean enable = true;

            private static final String BOUNDARY = "(?<!\\\\{2,})(?<=\\s|^)(%s)(?=\\s|$|\\p{Punct})";
            private static final String FORMAT_TEMPLATE = "(?<!\\\\{2,})(?<=\\s|^)%s([^\\n]*?)%s(?=\\s|$|\\p{Punct})";

            private Map<String, String> triggers = new LinkedHashMap<>() {
                {
                    // emoticons
                    put("smile", String.format(BOUNDARY, ":-?\\)"));
                    put("big_smile", String.format(BOUNDARY, ":-?D"));
                    put("sad", String.format(BOUNDARY, ":-?\\("));
                    put("ok_hand", String.format(BOUNDARY, "(?i):ok:"));
                    put("thumbs_up", String.format(BOUNDARY, ":\\+1:"));
                    put("thumbs_down", String.format(BOUNDARY, ":-1:"));
                    put("cool_smile", String.format(BOUNDARY, "(?i):cool:"));
                    put("cool_glasses", String.format(BOUNDARY, "B-?\\)"));
                    put("clown", String.format(BOUNDARY, "(?i):clown:"));
                    put("heart", String.format(BOUNDARY, "<3"));
                    put("laughing", String.format(BOUNDARY, "(?i)xd"));
                    put("confused", String.format(BOUNDARY, "%-?\\)"));
                    put("happy", String.format(BOUNDARY, "=D"));
                    put("angry", String.format(BOUNDARY, ">:-?\\("));

                    // ascii Art
                    put("ascii_idk", String.format(BOUNDARY, "(?i):idk:"));
                    put("ascii_angry", String.format(BOUNDARY, "(?i):angry:"));
                    put("ascii_happy", String.format(BOUNDARY, "(?i):happy:"));

                    // dynamic Placeholders
                    put("ping", String.format(BOUNDARY, "%ping%"));
                    put("tps", String.format(BOUNDARY, "%tps%"));
                    put("online", String.format(BOUNDARY, "%online%"));
                    put("coords", String.format(BOUNDARY, "%coords%"));
                    put("stats", String.format(BOUNDARY, "%stats%"));
                    put("skin", String.format(BOUNDARY, "%skin%"));
                    put("item", String.format(BOUNDARY, "%item%"));

                    // text formatting
                    put("image", "(?<!\\\\{2,})(?<=\\s|^)((?:https?|HTTPS?)://(?:[a-zA-Z0-9-]{1,63}\\.)*(?:imgur\\.com|discordapp\\.net|cdn\\.discordapp\\.com)/[\\w\\-./?=&%]*\\.(?:jpg|jpeg|png|gif|webp|bmp))(?!\\S)");
                    put("url", "(?<!\\\\{2,})(?<=\\s|^)((?:https?|ftp|HTTPS?)://(?:[\\p{L}a-zA-Z0-9-]{1,63}\\.)+[\\p{L}a-zA-Z]{2,6}(?::\\d{1,5})?(?:/[\\w\\-./?=&%]*)?)(?!\\S)");
                    put("spoiler", String.format(FORMAT_TEMPLATE, "\\|\\|", "\\|\\|"));
                    put("bold", String.format(FORMAT_TEMPLATE, "\\*\\*", "\\*\\*"));
                    put("italic", String.format(FORMAT_TEMPLATE, "\\*", "\\*"));
                    put("underline", String.format(FORMAT_TEMPLATE, "__", "__"));
                    put("obfuscated", String.format(FORMAT_TEMPLATE, "\\?\\?", "\\?\\?"));
                    put("strikethrough", String.format(FORMAT_TEMPLATE, "~~", "~~"));
                }
            };
        }

        @Getter
        public static final class Scoreboard implements SubFormatMessageConfig, Config.IEnable {
            private boolean enable = true;
            private boolean nameVisible = false;
            private String color = "<white>";
            private String prefix = "<vault_prefix><stream_prefix>";
            private String suffix = "<afk_suffix><vault_suffix>";
            private Ticker ticker = new Ticker();
        }

        @Getter
        public static final class Translate implements SubFormatMessageConfig, Config.IEnable {
            private boolean enable = true;
        }

        @Getter
        public static final class World implements SubFormatMessageConfig, Config.IEnable {
            private boolean enable = true;
            private WorldModule.Mode mode = WorldModule.Mode.TYPE;
            private Ticker ticker = new Ticker(false, 100);
            private Map<String, String> values = new LinkedHashMap<>(){
                {
                    put("normal", "<color:#98FB98>");
                    put("world", "<color:#98FB98>");
                    put("overworld", "<color:#98FB98>");
                    put("custom", "<color:#98FB98>");
                    put("nether", "<color:#F08080>");
                    put("the_nether", "<color:#F08080>");
                    put("world_nether", "<color:#F08080>");
                    put("the_end", "<color:#9370DB>");
                    put("world_the_end", "<color:#9370DB>");
                }
            };
        }
    }

    @Getter
    public static final class Gamemode implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Gamerule implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Give implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Greeting implements SubMessageConfig, Config.IEnable {
        private boolean enable = false;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Join implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private boolean first = true;
        private Range range = Range.get(Range.Type.SERVER);
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Kill implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Sound sound = new Sound();
        private Destination destination = new Destination();
    }

    @Getter
    public static final class Locate implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Sound sound = new Sound();
        private Destination destination = new Destination();
    }

    @Getter
    public static final class Objective implements ObjectiveMessageConfig, Config.IEnable {

        private boolean enable = true;

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/objective/belowname/")})
        private Belowname belowname = new Belowname();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/objective/tabname/")})
        private Tabname tabname = new Tabname();

        @Getter
        public static final class Belowname implements SubObjectiveMessageConfig {
            private boolean enable = false;
            private ObjectiveModule.Mode mode = ObjectiveModule.Mode.PING;
            private Ticker ticker = new Ticker(true, 100);
        }

        @Getter
        public static final class Tabname implements SubObjectiveMessageConfig {
            private boolean enable = false;
            private ObjectiveModule.Mode mode = ObjectiveModule.Mode.PING;
            private Ticker ticker = new Ticker(true, 100);
        }

    }

    @Getter
    public static final class Op implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Particle implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Quit implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Range range = Range.get(Range.Type.SERVER);
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Reload implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Ride implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Rightclick implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination(Destination.Type.ACTION_BAR, new Times(0, 60, 0));
        private Cooldown cooldown = new Cooldown();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Rotate implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Save implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Seed implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Setblock implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Sidebar implements SubMessageConfig, Config.IEnable {
        private boolean enable = false;
        private boolean random = true;
        private Ticker ticker = new Ticker(true, 100);
    }

    @Getter
    public static final class Sign implements SubMessageConfig, Config.IEnable {
        private boolean enable = false;
    }

    @Getter
    public static final class Sleep implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination(Destination.Type.ACTION_BAR);
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Spawn implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Status implements StatusMessageConfig, Config.IEnable {

        private boolean enable = true;

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/status/icon/")})
        private Icon icon = new Icon();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/status/motd/")})
        private MOTD motd = new MOTD();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/status/players/")})
        private Players players = new Players();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/status/version/")})
        private Version version = new Version();

        @Getter
        public static final class MOTD implements SubStatusMessageConfig, Config.IEnable {
            private boolean enable = true;
            private boolean random = true;
        }

        @Getter
        public static final class Icon implements SubStatusMessageConfig, Config.IEnable {
            private boolean enable = false;
            private boolean random = true;
            private List<String> values = new ArrayList<>(){
                {
                    add("server-icon-1.png");
                    add("server-icon-2.png");
                }
            };
        }

        @Getter
        public static final class Players implements SubStatusMessageConfig, Config.IEnable {
            private boolean enable = false;
            private boolean control = true;
            private int max = 69;
            private int online = -69;
        }

        @Getter
        public static final class Version implements SubStatusMessageConfig, Config.IEnable {
            private boolean enable = false;
            private int protocol = -1;
        }

    }

    @Getter
    public static final class Stop implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Summon implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Tab implements TabMessageConfig, Config.IEnable {

        private boolean enable = true;

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/tab/header/")})
        private Header header = new Header();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/tab/footer/")})
        private Footer footer = new Footer();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/tab/playerlistname/")})
        private Playerlistname playerlistname = new Playerlistname();

        @Getter
        public static final class Footer implements SubTabMessageConfig, Config.IEnable {
            private boolean enable = true;
            private boolean random = true;
            private Destination destination = new Destination(Destination.Type.TAB_FOOTER);
            private Ticker ticker = new Ticker(true, 100);
        }

        @Getter
        public static final class Header implements SubTabMessageConfig, Config.IEnable {
            private boolean enable = true;
            private boolean random = true;
            private Destination destination = new Destination(Destination.Type.TAB_HEADER);
            private Ticker ticker = new Ticker(true, 100);
        }

        @Getter
        public static final class Playerlistname implements SubTabMessageConfig, Config.IEnable {
            private boolean enable = true;
            private Ticker ticker = new Ticker(true, 100);
        }
    }

    @Getter
    public static final class Teleport implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Time implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Update implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Weather implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }
}
