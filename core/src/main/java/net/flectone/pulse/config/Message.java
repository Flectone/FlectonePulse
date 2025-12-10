package net.flectone.pulse.config;

import com.fasterxml.jackson.annotation.*;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.util.*;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.util.constant.AdventureTag;
import net.flectone.pulse.util.constant.MessageType;

import java.nio.file.Path;
import java.util.*;

@SuppressWarnings({"FieldMayBeFinal", "unused"})
@Getter
public final class Message extends YamlFile implements ModuleConfig.MessageConfig, Config.IEnable {

    public Message(Path projectPath) {
        super(projectPath.resolve("message.yml"));
    }

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/")
    private boolean enable = true;

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/afk/")
    private Afk afk = new Afk();

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/anvil/")
    private Anvil anvil = new Anvil();

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/auto/")
    private Auto auto = new Auto();

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/book/")
    private Book book = new Book();

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/bossbar/")
    private Bossbar bossbar = new Bossbar();

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
    public static final class Afk implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;

        private Range range = Range.get(Range.Type.SERVER);

        private int delay = 3000;

        @JsonMerge(OptBoolean.FALSE)
        private List<String> ignore = new ArrayList<>(List.of("afk"));

        private Destination destination = new Destination();
        private Ticker ticker = new Ticker(true, 20);
    }

    @Getter
    public static final class Anvil implements SubMessageConfig, Config.IEnable {
        private boolean enable = false;
    }

    @Getter
    public static final class Auto implements SubMessageConfig, Config.IEnable {
        private boolean enable = false;

        @JsonMerge(OptBoolean.FALSE)
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
    public static final class Book implements SubMessageConfig, Config.IEnable {
        private boolean enable = false;
    }

    @Getter
    public static final class Bossbar implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;

        @JsonMerge(OptBoolean.FALSE)
        private Map<String, Announce> announce = new LinkedHashMap<>() {
            {
                put("key", new Announce());
            }
        };

        @Getter
        @NoArgsConstructor
        public static final class Announce {
            private Destination destination = new Destination(Destination.Type.TITLE);
            private Sound sound = new Sound();
        }
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

        @JsonMerge(OptBoolean.FALSE)
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
            private boolean cancel;

            private Range range = Range.get(100);

            private int priority;
            private String trigger;
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
    public static final class Format implements FormatMessageConfig, Config.IEnable {

        private boolean enable = true;
        private boolean convertLegacyColor = true;

        @JsonMerge(OptBoolean.FALSE)
        private List<AdventureTag> adventureTags = new ArrayList<>(List.of(AdventureTag.values()));

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/fcolor/")
        private FColor fcolor = new FColor();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/fixation/")
        private Fixation fixation = new Fixation();

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

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/translate/")
        private Translate translate = new Translate();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/world/")
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

            @JsonMerge(OptBoolean.FALSE)
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
        public static final class Fixation implements SubFormatMessageConfig, Config.IEnable {
            private boolean enable = false;
            private boolean endDot = false;
            private boolean firstLetterUppercase = false;

            @JsonMerge(OptBoolean.FALSE)
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
            private String everyoneTag = "here";
            private Destination destination = new Destination(Destination.Type.TOAST, new Toast("minecraft:bell", Toast.Type.TASK));
            private Sound sound = new Sound(true, 0.1f, 0.1f, SoundCategory.NEUTRAL, Sounds.ENTITY_EXPERIENCE_ORB_PICKUP.getName().toString());
        }

        @Getter
        public static final class Moderation implements ModerationFormatMessageConfig, Config.IEnable {

            private boolean enable = true;

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

                @Setter
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

                @JsonMerge(OptBoolean.FALSE)
                private List<String> ignore = new ArrayList<>(List.of("тебя", "тебе"));

                @JsonMerge(OptBoolean.FALSE)
                private List<String> trigger = new ArrayList<>(List.of(
                        "((у|[нз]а|(хитро|не)?вз?[ыьъ]|с[ьъ]|(и|ра)[зс]ъ?|(о[тб]|под)[ьъ]?|(.\\B)+?[оаеи])?-?([её]б(?!о[рй])|и[пб][ае][тц]).*?|(н[иеа]|([дп]|верт)о|ра[зс]|з?а|с(ме)?|о(т|дно)?|апч)?-?ху([яйиеёю]|ли(?!ган)).*?|(в[зы]|(три|два|четыре)жды|(н|сук)а)?-?бл(я(?!(х|ш[кн]|мб)[ауеыио]).*?|[еэ][дт]ь?)|(ра[сз]|[зн]а|[со]|вы?|п(ере|р[оие]|од)|и[зс]ъ?|[ао]т)?п[иеё]зд.*?|(за)?п[ие]д[аое]?р([оа]м|(ас)?(ну.*?|и(ли)?[нщктл]ь?)?|(о(ч[еи])?|ас)?к(ой)|юг)[ауеы]?|манд([ауеыи](л(и[сзщ])?[ауеиы])?|ой|[ао]вошь?(е?к[ауе])?|юк(ов|[ауи])?)|муд([яаио].*?|е?н([ьюия]|ей))|мля([тд]ь)?|лять|([нз]а|по|пи)х|м[ао]л[ао]фь([яию]|[еёо]й))(?=[\\s,.:;\"']|$)",
                        "(([уyu]|[нзnz3][аa]|(хитро|не)?[вvwb][зz3]?[ыьъi]|[сsc][ьъ']|(и|[рpr][аa4])[зсzs]ъ?|([оo0][тбtb6]|[пp][оo0][дd9])[ьъ']?|(.\\B)+?[оаеиeo])?-?([еёe][бb6](?!о[рй])|и[пб][ае][тц]).*?|([нn][иеаaie]|([дпdp]|[вv][еe3][рpr][тt])[оo0]|[рpr][аa][зсzc3]|[з3z]?[аa]|с(ме)?|[оo0]([тt]|дно)?|апч)?-?[хxh][уuy]([яйиеёюuie]|ли(?!ган)).*?|([вvw][зы3z]|(три|два|четыре)жды|(н|[сc][уuy][кk])[аa])?-?[бb6][лl]([яy](?!(х|ш[кн]|мб)[ауеыио]).*?|[еэe][дтdt][ь']?)|([рp][аa][сзc3z]|[знzn][аa]|[соsc]|[вv][ыi]?|[пp]([еe][рpr][еe]|[рrp][оиioеe]|[оo0][дd])|и[зс]ъ?|[аоao][тt])?[пpn][иеёieu][зz3][дd9].*?|([зz3][аa])?[пp][иеieu][дd][аоеaoe]?[рrp](ну.*?|[оаoa][мm]|([аa][сcs])?([иiu]([лl][иiu])?[нщктлtlsn]ь?)?|([оo](ч[еиei])?|[аa][сcs])?[кk]([оo]й)?|[юu][гg])[ауеыauyei]?|[мm][аa][нnh][дd]([ауеыayueiи]([лl]([иi][сзc3щ])?[ауеыauyei])?|[оo][йi]|[аоao][вvwb][оo](ш|sh)[ь']?([e]?[кk][ауеayue])?|юк(ов|[ауи])?)|[мm][уuy][дd6]([яyаиоaiuo0].*?|[еe]?[нhn]([ьюия'uiya]|ей))|мля([тд]ь)?|лять|([нз]а|по|пи)х|м[ао]л[ао]фь([яию]|[её]й))(?=[\\s,.:;\"']|$)")
                );
            }

        }

        @Getter
        public static final class Names implements SubFormatMessageConfig, Config.IEnable {
            private boolean enable = true;
            private boolean shouldCheckInvisibility = false;
        }

        @Getter
        public static final class Object implements SubFormatMessageConfig, Config.IEnable {
            private boolean enable = true;
            private boolean playerHead = true;
            private boolean sprite = true;
            private boolean needExtraSpace = true;
        }

        @Getter
        public static final class QuestionAnswer implements SubFormatMessageConfig, Config.IEnable {
            private boolean enable = false;

            @JsonMerge(OptBoolean.FALSE)
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
            private boolean usePaperDataComponents = false;

            private static final String BOUNDARY = "(?<!\\\\)(?<!\\S)%s(?!\\S)";
            private static final String FORMAT_TEMPLATE = "(?<!\\S)%1$s([^%1$s\\n]+)%1$s(?!\\S)";

            @JsonMerge(OptBoolean.FALSE)
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
                    put("spoiler", String.format(FORMAT_TEMPLATE, "\\|\\|"));
                    put("bold", String.format(FORMAT_TEMPLATE, "\\*\\*"));
                    put("italic", String.format(FORMAT_TEMPLATE, "\\*"));
                    put("underline", String.format(FORMAT_TEMPLATE, "__"));
                    put("obfuscated", String.format(FORMAT_TEMPLATE, "\\?\\?"));
                    put("strikethrough", String.format(FORMAT_TEMPLATE, "~~"));
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

            @JsonMerge(OptBoolean.FALSE)
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
    public static final class Objective implements ObjectiveMessageConfig, Config.IEnable {

        private boolean enable = true;

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/objective/belowname/")
        private Belowname belowname = new Belowname();
        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/objective/tabname/")
        private Tabname tabname = new Tabname();

        @Getter
        public static final class Belowname implements SubObjectiveMessageConfig, Config.IEnable {
            private boolean enable = false;
            private ObjectiveModule.Mode mode = ObjectiveModule.Mode.PING;
            private Ticker ticker = new Ticker(true, 100);
        }

        @Getter
        public static final class Tabname implements SubObjectiveMessageConfig, Config.IEnable {
            private boolean enable = false;
            private ObjectiveModule.Mode mode = ObjectiveModule.Mode.PING;
            private Ticker ticker = new Ticker(true, 100);
        }

    }

    @Getter
    public static final class Quit implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;

        private Range range = Range.get(Range.Type.SERVER);

        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Rightclick implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;
        private boolean shouldCheckSneaking = false;
        private boolean hideNameWhenInvisible = true;

        private Range range = Range.get(Range.Type.PLAYER);

        private Destination destination = new Destination(Destination.Type.ACTION_BAR, new Times(0, 60, 0));
        private Cooldown cooldown = new Cooldown();
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
    public static final class Status implements StatusMessageConfig, Config.IEnable {

        private boolean enable = true;

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/icon/")
        private Icon icon = new Icon();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/motd/")
        private MOTD motd = new MOTD();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/players/")
        private Players players = new Players();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/version/")
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

            @JsonMerge(OptBoolean.FALSE)
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
    public static final class Tab implements TabMessageConfig, Config.IEnable {

        private boolean enable = true;

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/header/")
        private Header header = new Header();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/footer/")
        private Footer footer = new Footer();

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/playerlistname/")
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
            private boolean proxyMode = true;
            private Ticker ticker = new Ticker(true, 100);
        }
    }

    @Getter
    public static final class Update implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;

        @JsonMerge(OptBoolean.FALSE)
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Vanilla implements SubMessageConfig, Config.IEnable {
        private boolean enable = true;

        @JsonMerge(OptBoolean.FALSE)
        private List<VanillaMessage> types = new LinkedList<>() {
            {
                add(new VanillaMessage(null, new Destination(Destination.Type.ACTION_BAR, new Times(0, 20, 0)), List.of(
                        "block.minecraft.bed.no_sleep",
                        "block.minecraft.bed.not_safe", "block.minecraft.bed.obstructed", "block.minecraft.bed.occupied", "block.minecraft.bed.too_far_away", "tile.bed.noSleep", "tile.bed.notSafe",
                        "tile.bed.notValid", "tile.bed.occupied", "block.minecraft.spawn.not_valid", "block.minecraft.bed.not_valid",
                        "item.minecraft.debug_stick.empty", "item.minecraft.debug_stick.select", "item.minecraft.debug_stick.update"
                )));
                add(new VanillaMessage(MessageType.SLEEP.name(), new Destination(Destination.Type.ACTION_BAR, new Times(0, 20, 0)), List.of(
                        "sleep.not_possible", "sleep.players_sleeping", "sleep.skipping_night"
                )));
                add(new VanillaMessage(MessageType.ADVANCEMENT.name(), true, Range.get(Range.Type.SERVER), List.of(
                        "chat.type.advancement.challenge", "chat.type.advancement.goal", "chat.type.advancement.task",
                        "chat.type.achievement", "chat.type.achievement.taken"
                )));
                add(new VanillaMessage(MessageType.DEATH.name(), true, Range.get(Range.Type.SERVER), List.of(
                        "death.attack.anvil", "death.attack.anvil.player", "death.attack.arrow", "death.attack.arrow.item", "death.attack.badRespawnPoint.message", "death.attack.cactus",
                        "death.attack.cactus.player", "death.attack.cramming", "death.attack.cramming.player", "death.attack.dragonBreath", "death.attack.dragonBreath.player", "death.attack.drown",
                        "death.attack.drown.player", "death.attack.dryout", "death.attack.dryout.player", "death.attack.even_more_magic", "death.attack.explosion", "death.attack.explosion.player",
                        "death.attack.explosion.item", "death.attack.explosion.player.item", "death.attack.fall", "death.attack.fall.player", "death.attack.fallingBlock", "death.attack.fallingBlock.player",
                        "death.attack.fallingStalactite", "death.attack.fallingStalactite.player", "death.attack.fireball", "death.attack.fireball.item", "death.attack.fireworks",
                        "death.attack.fireworks.item", "death.attack.fireworks.player", "death.attack.flyIntoWall", "death.attack.flyIntoWall.player", "death.attack.freeze", "death.attack.freeze.player",
                        "death.attack.generic", "death.attack.generic.player", "death.attack.genericKill", "death.attack.genericKill.player", "death.attack.hotFloor", "death.attack.hotFloor.player",
                        "death.attack.inFire", "death.attack.inFire.player", "death.attack.inWall", "death.attack.inWall.player", "death.attack.indirectMagic", "death.attack.indirectMagic.item", "death.attack.lava",
                        "death.attack.lava.player", "death.attack.lightningBolt", "death.attack.lightningBolt.player", "death.attack.mace_smash", "death.attack.mace_smash.item", "death.attack.magic",
                        "death.attack.magic.player", "death.attack.mob", "death.attack.mob.item", "death.attack.onFire", "death.attack.onFire.item", "death.attack.onFire.player", "death.attack.outOfWorld",
                        "death.attack.outOfWorld.player", "death.attack.outsideBorder", "death.attack.outsideBorder.player", "death.attack.player", "death.attack.player.item", "death.attack.sonic_boom",
                        "death.attack.sonic_boom.item", "death.attack.sonic_boom.player", "death.attack.stalagmite", "death.attack.stalagmite.player", "death.attack.starve", "death.attack.starve.player", "death.attack.sting", "death.attack.sting.item",
                        "death.attack.sting.player", "death.attack.sweetBerryBush", "death.attack.sweetBerryBush.player", "death.attack.thorns", "death.attack.thorns.item", "death.attack.thrown", "death.attack.thrown.item", "death.attack.trident", "death.attack.trident.item", "death.attack.wither",
                        "death.attack.wither.player", "death.attack.witherSkull", "death.attack.witherSkull.item", "death.fell.accident.generic", "death.fell.accident.ladder", "death.fell.accident.other_climbable", "death.fell.accident.scaffolding",
                        "death.fell.accident.twisting_vines", "death.fell.accident.vines", "death.fell.accident.weeping_vines", "death.fell.assist", "death.fell.assist.item", "death.fell.finish", "death.fell.finish.item", "death.fell.killer"
                )));
            }
        };

        @Getter
        @NoArgsConstructor
        public static final class VanillaMessage {

            private boolean multiMessage = false;
            private String name = "";
            private Range range = Range.get(Range.Type.PLAYER);
            private Destination destination = new Destination();
            private Sound sound = new Sound();

            @JsonMerge(OptBoolean.FALSE)
            private List<String> translationKeys = new ArrayList<>();

            @JsonCreator
            public VanillaMessage(@JsonProperty("name") String name,
                                  @JsonProperty("multi_message") Boolean multiMessage,
                                  @JsonProperty("range") Range range,
                                  @JsonProperty("destination") Destination destination,
                                  @JsonProperty("sound") Sound sound,
                                  @JsonProperty("translation_keys") List<String> translationKeys) {
                this.name = name != null ? name : "";
                this.multiMessage = multiMessage != null ? multiMessage : false;
                this.range = range != null ? range : Range.get(Range.Type.PLAYER);
                this.destination = destination != null ? destination : new Destination();
                this.sound = sound != null ? sound : new Sound();
                this.translationKeys = translationKeys != null ? new LinkedList<>(translationKeys) : new LinkedList<>();
            }

            @JsonValue
            public Map<String, Object> toJson() {
                Map<String, Object> result = new LinkedHashMap<>();

                if (!name.isEmpty()) {
                    result.put("name", name.toUpperCase());
                }

                if (multiMessage) {
                    result.put("multi_message", true);
                }

                if (range.getType() != Range.Type.PLAYER) {
                    result.put("range", range);
                }

                if (destination.getType() != Destination.Type.CHAT) {
                    result.put("destination", destination);
                }

                if (sound.isEnable()) {
                    result.put("sound", sound);
                }

                if (!translationKeys.isEmpty()) {
                    result.put("translation_keys", translationKeys);
                }

                return result;
            }

            public VanillaMessage(String name, Destination destination, List<String> translationKeys) {
                this(name, null, null, destination, null, translationKeys);
            }

            public VanillaMessage(String name, boolean multiMessage, Range range, List<String> translationKeys) {
                this(name, multiMessage, range, null, null, translationKeys);
            }

        }
    }
}
