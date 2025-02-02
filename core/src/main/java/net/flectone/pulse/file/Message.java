package net.flectone.pulse.file;

import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.flectone.pulse.model.*;
import net.flectone.pulse.module.message.format.world.WorldMode;
import net.flectone.pulse.module.message.objective.ObjectiveMode;
import net.flectone.pulse.util.AdvancementType;
import net.flectone.pulse.util.Range;
import net.flectone.pulse.util.TagType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
public final class Message extends FileSerializable implements IModule.IMessage {

    public Message(Path projectPath) {
        super(projectPath.resolve("message.yml"));
    }

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/")})
    private boolean enable = true;

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/advancement/")})
    private Advancement advancement = new Advancement();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/anvil/")})
    private Anvil anvil = new Anvil();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/auto/")})
    private Auto auto = new Auto();
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
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/contact/")})
    private Contact contact = new Contact();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/death/")})
    private Death death = new Death();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/deop/")})
    private Deop deop = new Deop();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/enchant/")})
    private Enchant enchant = new Enchant();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/")})
    private Format format = new Format();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/gamemode/")})
    private Gamemode gamemode = new Gamemode();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/greeting/")})
    private Greeting greeting = new Greeting();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/join/")})
    private Join join = new Join();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/objective/")})
    private Objective objective = new Objective();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/op/")})
    private Op op = new Op();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/quit/")})
    private Quit quit = new Quit();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/scoreboard/")})
    private Scoreboard scoreboard = new Scoreboard();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/seed/")})
    private Seed seed = new Seed();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/setblock/")})
    private Setblock setblock = new Setblock();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/sign/")})
    private Sign sign = new Sign();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/spawnpoint/")})
    private Spawnpoint spawnpoint = new Spawnpoint();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/status/")})
    private Status status = new Status();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/tab/")})
    private Tab tab = new Tab();

    @Getter
    public static final class Advancement implements ISubMessage, Config.IEnable {
        private boolean enable = true;
        private boolean grant = true;
        private boolean revoke = true;
        private int range = Range.SERVER;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Anvil implements ISubMessage, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class Auto implements ISubMessage, Config.IEnable {
        private boolean enable = false;
        private boolean random = true;
        private Destination destination = new Destination();
        private Ticker ticker = new Ticker(true, 9000);
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Book implements ISubMessage, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class Brand implements ISubMessage, Config.IEnable {
        private boolean enable = true;
        private boolean random = true;
        private Destination destination = new Destination(Destination.Type.BRAND);
        private Ticker ticker = new Ticker(true, 100);
    }

    @Getter
    public static final class Bubble implements ISubMessage, Config.IEnable {
        private boolean enable = true;
        private double distance = 30.0;
        private int lineWidth = 200;
        private double readSpeed = 100.0;
        private double handicapChars = 10.0;
        private Modern modern = new Modern();

        @Getter
        @NoArgsConstructor
        public static final class Modern {
            private boolean enable = true;
            private boolean hasShadow = false;
            private float height = 0.2f;
            private float scale = 1.0f;
            private String background = "#00000040";
        }
    }

    @Getter
    public static final class Chat implements ISubMessage, Config.IEnable {
        private boolean enable = true;
        private Map<String, Type> types = new LinkedHashMap<>(){
            {
                put("local", new Type("", true, true, 100, 0));
                put("global", new Type("!", true, false, Range.PROXY, 5));
            }
        };

        @Getter
        @NoArgsConstructor
        public static final class Type {
            private boolean enable = true;
            private boolean nullRecipient = false;
            private boolean cancel = false;
            private int range = 0;
            private int priority = 0;
            private String trigger = "";
            private Destination destination = new Destination();
            private Cooldown cooldown = new Cooldown();
            private Sound sound = new Sound();

            public Type(String trigger, boolean nullRecipient, boolean cancel, int range, int priority) {
                this.trigger = trigger;
                this.nullRecipient = nullRecipient;
                this.cancel = cancel;
                this.range = range;
                this.priority = priority;
            }
        }
    }

    @Getter
    public static final class Clear implements ISubMessage, Config.IEnable {
        private boolean enable = true;
        private Sound sound = new Sound();
        private Destination destination = new Destination();
    }

    @Getter
    public static final class Contact implements IContactMessage, Config.IEnable {

        private boolean enable = true;

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/contact/afk/")})
        private Afk afk = new Afk();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/contact/knock/")})
        private Knock knock = new Knock();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/contact/mark/")})
        private Mark mark = new Mark();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/contact/rightclick/")})
        private Rightclick rightclick = new Rightclick();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/contact/sign/")})
        private Sign sign = new Sign();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/contact/spit/")})
        private Spit spit = new Spit();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/contact/unsign/")})
        private Unsign unsign = new Unsign();

        @Getter
        public static final class Afk implements ISubContactMessage, Config.IEnable {
            private boolean enable = true;
            private int range = Range.SERVER;
            private int delay = 3000;
            private List<String> ignore = new ArrayList<>(List.of("afk"));
            private Destination destination = new Destination();
            private Ticker ticker = new Ticker(true, 20);
        }

        @Getter
        public static final class Knock implements ISubContactMessage, Config.IEnable {
            private boolean enable = false;
            private Cooldown cooldown = new Cooldown();
            private Map<String, Sound> types = new LinkedHashMap<>(){
                {
                    put("GLASS", new Sound(true, 1f, 1f, SoundCategory.BLOCK.name(), Sounds.BLOCK_GLASS_PLACE.getName().toString()));
                    put("DOOR", new Sound(true, 1f, 1f, SoundCategory.BLOCK.name(), Sounds.BLOCK_WOOD_PLACE.getName().toString()));
                }
            };
        }

        @Getter
        public static final class Mark implements ISubContactMessage, Config.IEnable {
            private boolean enable = false;
            private boolean limit = true;
            private boolean color = true;
            private int range = 100;
            private int duration = 60;
            private String item = "WOODEN_SWORD";
            private String entity = "MAGMA_CUBE";
            private Cooldown cooldown = new Cooldown();
            private Sound sound = new Sound();
        }

        @Getter
        public static final class Rightclick implements ISubContactMessage, Config.IEnable {
            private boolean enable = true;
            private Destination destination = new Destination(Destination.Type.ACTION_BAR);
            private Cooldown cooldown = new Cooldown();
            private Sound sound = new Sound();
        }

        @Getter
        public static final class Sign implements ISubContactMessage, Config.IEnable {
            private boolean enable = false;
            private boolean dropDye = true;
            private String block = "ANVIL";
            private Cooldown cooldown = new Cooldown();
            private Sound sound = new Sound();
        }

        @Getter
        public static final class Spit implements ISubContactMessage, Config.IEnable {
            private boolean enable = false;
            private boolean message = true;
            private String item = "WHITE_DYE";
            private Destination destination = new Destination(Destination.Type.ACTION_BAR);
            private Cooldown cooldown = new Cooldown(true, 60);
            private Sound sound = new Sound(true, 0.3f, 1f, SoundCategory.HOSTILE.name(), Sounds.ENTITY_LLAMA_SPIT.getName().toString());
        }

        @Getter
        public static final class Unsign implements ISubContactMessage, Config.IEnable {
            private boolean enable = false;
            private boolean dropDye = true;
            private String block = "GRINDSTONE";
            private Cooldown cooldown = new Cooldown();
            private Sound sound = new Sound();
        }
    }

    @Getter
    public static final class Death implements ISubMessage, Config.IEnable {
        private boolean enable = true;
        private int range = Range.SERVER;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Deop implements ISubMessage, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Enchant implements ISubMessage, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Format implements IFormatMessage, Config.IEnable {

        private boolean enable = true;

        private Map<TagType, Tag> tags = new LinkedHashMap<>(){
            {
                put(TagType.PING, new Tag("%ping%"));
                put(TagType.TPS, new Tag("%tps%"));
                put(TagType.ONLINE, new Tag("%online%"));
                put(TagType.COORDS, new Tag("%coords%"));
                put(TagType.STATS, new Tag("%stats%"));
                put(TagType.SKIN, new Tag("%skin%"));
                put(TagType.ITEM, new Tag("%item%"));
                put(TagType.URL, new Tag("(?<!:\")((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w:#@%/;$()~_?+-=\\\\.&]*)"));
                put(TagType.IMAGE, new Tag("(?<!:\")((https?|ftp|gopher|telnet|file):\\/\\/(?:i\\.imgur\\.com|media\\.discordapp\\.net)\\/[\\w:#@%/;$()~_?+-=\\\\.&]*)"));
                put(TagType.SPOILER, new Tag(Pattern.quote("||")));
                put(TagType.BOLD, new Tag(Pattern.quote("**")));
                put(TagType.ITALIC, new Tag(Pattern.quote("##")));
                put(TagType.UNDERLINE, new Tag(Pattern.quote("__")));
                put(TagType.OBFUSCATED, new Tag(Pattern.quote("??")));
                put(TagType.STRIKETHROUGH, new Tag(Pattern.quote("~~")));
                put(TagType.HOVER, new KyoriTag());
                put(TagType.CLICK, new KyoriTag());
                put(TagType.COLOR, new KyoriTag());
                put(TagType.KEYBIND, new KyoriTag());
                put(TagType.TRANSLATABLE, new KyoriTag());
                put(TagType.TRANSLATABLE_FALLBACK, new KyoriTag());
                put(TagType.INSERTION, new KyoriTag());
                put(TagType.FONT, new KyoriTag());
                put(TagType.DECORATION, new KyoriTag());
                put(TagType.GRADIENT, new KyoriTag());
                put(TagType.RAINBOW, new KyoriTag());
                put(TagType.RESET, new KyoriTag());
                put(TagType.NEWLINE, new KyoriTag());
                put(TagType.TRANSITION, new KyoriTag());
                put(TagType.SELECTOR, new KyoriTag());
                put(TagType.SCORE, new KyoriTag());
                put(TagType.NBT, new KyoriTag());
                put(TagType.PRIDE, new KyoriTag());
                put(TagType.SHADOW_COLOR, new KyoriTag());
            }
        };

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/color/")})
        private Color color = new Color();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/emoji/")})
        private Emoji emoji = new Emoji();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/image/")})
        private Image image = new Image();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/mention/")})
        private Mention mention = new Mention();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/moderation/")})
        private Moderation moderation = new Moderation();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/name_/")})
        private Name name_ = new Name();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/questionanswer/")})
        private QuestionAnswer questionAnswer = new QuestionAnswer();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/spoiler/")})
        private Spoiler spoiler = new Spoiler();
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
        public static final class KyoriTag extends Tag {
            private boolean enable = true;
            private String trigger = null;
        }

        @Getter
        public static final class Color implements ISubFormatMessage, Config.IEnable {
            private boolean enable = true;
            private boolean useRecipientColors = true;
            private Map<String, String> values = new LinkedHashMap<>(){
                {
                    put("1", "#ADD8E6");
                    put("2", "#87CEFA");
                    put("3", "#A9A9A9");
                    put("4", "#FFFAFA");
                }
            };
        }

        @Getter
        public static final class Emoji implements ISubFormatMessage, Config.IEnable {
            private boolean enable = true;
            private Map<String, String> values = new LinkedHashMap<>(){
                {
                    put(":)", "<click:suggest_command:\":)\"><hover:show_text:\":)\">☺</hover></click>");
                    put(":D", "<click:suggest_command:\":D\"><hover:show_text:\":D\">☻</hover></click>");
                    put(":(", "<click:suggest_command:\":(\"><hover:show_text:\":(\">☹</hover></click>");
                    put(":ok:", "<click:suggest_command:\":ok:\"><hover:show_text:\":ok:\">\uD83D\uDD92</hover></click>");
                    put(":+1:", "<click:suggest_command:\":+1:\"><hover:show_text:\":+1:\">\uD83D\uDD92</hover></click>");
                    put(":-1:", "<click:suggest_command:\":-1:\"><hover:show_text:\":-1:\">\uD83D\uDD93</hover></click>");
                    put(":cool:", "<click:suggest_command:\":cool:\"><hover:show_text:\":cool:\">\uD83D\uDE0E</hover></click>");
                    put("B)", "<click:suggest_command:\"B)\"><hover:show_text:\"B)\">\uD83D\uDE0E</hover></click>");
                    put(":clown:", "<click:suggest_command:\":clown:\"><hover:show_text:\":clown:\">\uD83E\uDD21</hover></click>");
                    put("<3", "<click:suggest_command:\"<3\"><hover:show_text:\"<3\">❤</hover></click>");
                    put("XD", "<click:suggest_command:\"XD\"><hover:show_text:\"XD\">\uD83D\uDE06</hover></click>");
                    put("%)", "<click:suggest_command:\"%)\"><hover:show_text:\"%)\">\uD83D\uDE35</hover></click>");
                    put("=D", "<click:suggest_command:\"=D\"><hover:show_text:\"=D\">\uD83D\uDE03</hover></click>");
                    put(">:(", "<click:suggest_command:\">:(\"><hover:show_text:\">:(\">\uD83D\uDE21</hover></click>");
                    put(":idk:", "<click:suggest_command:\":idk:\"><hover:show_text:\":idk:\">¯\\_(ツ)_/¯</hover></click>");
                    put(":angry:", "<click:suggest_command:\":angry:\"><hover:show_text:\":angry:\">(╯°□°)╯︵ ┻━┻</hover></click>");
                    put(":happy:", "<click:suggest_command:\":happy:\"><hover:show_text:\":happy:\">＼(＾O＾)／</hover></click>");
                }
            };
        }

        @Getter
        public static final class Image implements ISubFormatMessage, Config.IEnable {
            private boolean enable = true;
            private String color = "<fcolor:2>";
        }

        @Getter
        public static final class Mention implements ISubFormatMessage, Config.IEnable {
            private boolean enable = true;
            private String trigger = "@";
            private Destination destination = new Destination(Destination.Type.TOAST, new Toast("minecraft:bell", AdvancementType.TASK));
            private Sound sound = new Sound(true, 0.1f, 0.1f, SoundCategory.NEUTRAL.name(), Sounds.ENTITY_EXPERIENCE_ORB_PICKUP.getName().toString());
        }

        @Getter
        public static final class Moderation implements IModerationFormatMessage, Config.IEnable {

            private boolean enable = true;

            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/moderation/caps/")})
            private Caps caps = new Caps();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/moderation/swear/")})
            private Swear swear = new Swear();

            @Getter
            public static final class Caps implements ISubModerationFormatMessage, Config.IEnable {
                private boolean enable = false;
                private double trigger = 0.7;
                private Sound sound = new Sound();
            }

            @Getter
            public static final class Swear implements ISubModerationFormatMessage, Config.IEnable {
                private boolean enable = false;
                private List<String> trigger = new ArrayList<>(List.of(
                        "((у|[нз]а|(хитро|не)?вз?[ыьъ]|с[ьъ]|(и|ра)[зс]ъ?|(о[тб]|под)[ьъ]?|(.\\B)+?[оаеи])?-?([её]б(?!о[рй])|и[пб][ае][тц]).*?|(н[иеа]|([дп]|верт)о|ра[зс]|з?а|с(ме)?|о(т|дно)?|апч)?-?ху([яйиеёю]|ли(?!ган)).*?|(в[зы]|(три|два|четыре)жды|(н|сук)а)?-?бл(я(?!(х|ш[кн]|мб)[ауеыио]).*?|[еэ][дт]ь?)|(ра[сз]|[зн]а|[со]|вы?|п(ере|р[оие]|од)|и[зс]ъ?|[ао]т)?п[иеё]зд.*?|(за)?п[ие]д[аое]?р([оа]м|(ас)?(ну.*?|и(ли)?[нщктл]ь?)?|(о(ч[еи])?|ас)?к(ой)|юг)[ауеы]?|манд([ауеыи](л(и[сзщ])?[ауеиы])?|ой|[ао]вошь?(е?к[ауе])?|юк(ов|[ауи])?)|муд([яаио].*?|е?н([ьюия]|ей))|мля([тд]ь)?|лять|([нз]а|по|пи)х|м[ао]л[ао]фь([яию]|[еёо]й))(?=[\\s,.:;\"']|$)",
                        "(([уyu]|[нзnz3][аa]|(хитро|не)?[вvwb][зz3]?[ыьъi]|[сsc][ьъ']|(и|[рpr][аa4])[зсzs]ъ?|([оo0][тбtb6]|[пp][оo0][дd9])[ьъ']?|(.\\B)+?[оаеиeo])?-?([еёe][бb6](?!о[рй])|и[пб][ае][тц]).*?|([нn][иеаaie]|([дпdp]|[вv][еe3][рpr][тt])[оo0]|[рpr][аa][зсzc3]|[з3z]?[аa]|с(ме)?|[оo0]([тt]|дно)?|апч)?-?[хxh][уuy]([яйиеёюuie]|ли(?!ган)).*?|([вvw][зы3z]|(три|два|четыре)жды|(н|[сc][уuy][кk])[аa])?-?[бb6][лl]([яy](?!(х|ш[кн]|мб)[ауеыио]).*?|[еэe][дтdt][ь']?)|([рp][аa][сзc3z]|[знzn][аa]|[соsc]|[вv][ыi]?|[пp]([еe][рpr][еe]|[рrp][оиioеe]|[оo0][дd])|и[зс]ъ?|[аоao][тt])?[пpn][иеёieu][зz3][дd9].*?|([зz3][аa])?[пp][иеieu][дd][аоеaoe]?[рrp](ну.*?|[оаoa][мm]|([аa][сcs])?([иiu]([лl][иiu])?[нщктлtlsn]ь?)?|([оo](ч[еиei])?|[аa][сcs])?[кk]([оo]й)?|[юu][гg])[ауеыauyei]?|[мm][аa][нnh][дd]([ауеыayueiи]([лl]([иi][сзc3щ])?[ауеыauyei])?|[оo][йi]|[аоao][вvwb][оo](ш|sh)[ь']?([e]?[кk][ауеayue])?|юк(ов|[ауи])?)|[мm][уuy][дd6]([яyаиоaiuo0].*?|[еe]?[нhn]([ьюия'uiya]|ей))|мля([тд]ь)?|лять|([нз]а|по|пи)х|м[ао]л[ао]фь([яию]|[её]й))(?=[\\s,.:;\"']|$)")
                );
                private Sound sound = new Sound();
            }

        }

        @Getter
        public static final class Name implements ISubFormatMessage, Config.IEnable {
            private boolean enable = true;
            private boolean team = true;
            private boolean visible = false;
            private String color = "<white>";
        }

        @Getter
        public static final class QuestionAnswer implements ISubFormatMessage, Config.IEnable {
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
                private int range = Range.PLAYER;
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
        public static final class Spoiler implements ISubFormatMessage, Config.IEnable {
            private boolean enable = true;
            private String color = "<fcolor:2>";
        }

        @Getter
        public static final class World implements ISubFormatMessage, Config.IEnable {
            private boolean enable = true;
            private WorldMode mode = WorldMode.TYPE;
            private Map<String, String> values = new LinkedHashMap<>(){
                {
                    put("normal", "<color:#98FB98>");
                    put("nether", "<color:#F08080>");
                    put("the_end", "<color:#9370DB>");
                    put("custom", "<color:#98FB98>");
                    put("world", "<color:#98FB98>");
                    put("world_nether", "<color:#F08080>");
                    put("world_the_end", "<color:#9370DB>");
                }
            };
        }
    }

    @Getter
    public static final class Gamemode implements ISubMessage, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Greeting implements ISubMessage, Config.IEnable {
        private boolean enable = false;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Join implements ISubMessage, Config.IEnable {
        private boolean enable = true;
        private boolean first = true;
        private int range = Range.SERVER;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Objective implements IObjectiveMessage, Config.IEnable {

        private boolean enable = true;

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/objective/belowname/")})
        private Belowname belowname = new Belowname();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/objective/tabname/")})
        private Tabname tabname = new Tabname();

        @Getter
        public static final class Belowname implements ISubObjectiveMessage {
            private boolean enable = false;
            private ObjectiveMode mode = ObjectiveMode.PING;
            private Ticker ticker = new Ticker(true, 100);
        }

        @Getter
        public static final class Tabname implements ISubObjectiveMessage {
            private boolean enable = false;
            private ObjectiveMode mode = ObjectiveMode.PING;
            private Ticker ticker = new Ticker(true, 100);
        }

    }

    @Getter
    public static final class Op implements ISubMessage, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Quit implements ISubMessage, Config.IEnable {
        private boolean enable = true;
        private int range = Range.SERVER;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Scoreboard implements ISubMessage, Config.IEnable {
        private boolean enable = false;
        private boolean random = true;
        private Ticker ticker = new Ticker(true, 100);
    }

    @Getter
    public static final class Seed implements ISubMessage, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Setblock implements ISubMessage, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Sign implements ISubMessage, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
    }

    @Getter
    public static final class Spawnpoint implements ISubMessage, Config.IEnable {
        private boolean enable = true;
        private Destination destination = new Destination();
        private Sound sound = new Sound();
    }

    @Getter
    public static final class Status implements IStatusMessage, Config.IEnable {

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
        public static final class MOTD implements ISubStatusMessage, Config.IEnable {
            private boolean enable = true;
            private boolean random = true;
        }

        @Getter
        public static final class Icon implements ISubStatusMessage, Config.IEnable {
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
        public static final class Players implements ISubStatusMessage, Config.IEnable {
            private boolean enable = false;
            private boolean control = true;
            private int max = 69;
            private int online = -69;
        }

        @Getter
        public static final class Version implements ISubStatusMessage, Config.IEnable {
            private boolean enable = false;
            private int protocol = -1;
        }

    }

    @Getter
    public static final class Tab implements ITabMessage, Config.IEnable {

        private boolean enable = true;

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/tab/header/")})
        private Header header = new Header();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/tab/footer/")})
        private Footer footer = new Footer();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/tab/playerlistname/")})
        private Playerlistname playerlistname = new Playerlistname();

        @Getter
        public static final class Footer implements ISubTabMessage, Config.IEnable {
            private boolean enable = true;
            private boolean random = true;
            private Destination destination = new Destination(Destination.Type.TAB_FOOTER);
            private Ticker ticker = new Ticker(true, 100);
        }

        @Getter
        public static final class Header implements ISubTabMessage, Config.IEnable {
            private boolean enable = true;
            private boolean random = true;
            private Destination destination = new Destination(Destination.Type.TAB_HEADER);
            private Ticker ticker = new Ticker(true, 100);
        }

        @Getter
        public static final class Playerlistname implements ISubTabMessage, Config.IEnable {
            private boolean enable = true;
            private Ticker ticker = new Ticker(true, 100);
        }
    }
}
