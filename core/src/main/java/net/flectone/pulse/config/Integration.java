package net.flectone.pulse.config;

import lombok.Getter;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.util.constant.MessageType;

import java.nio.file.Path;
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
public final class Integration extends FileSerializable implements ModuleConfig.IntegrationConfig {

    public Integration(Path projectPath) {
        super(projectPath.resolve("integration.yml"));
    }

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/")})
    private boolean enable = true;

    private String avatarApiUrl = "https://mc-heads.net/avatar/<skin>/8.png";
    private String bodyApiUrl = "https://mc-heads.net/player/<skin>/16.png";

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/advancedban/")})
    private Advancedban advancedban = new Advancedban();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/deepl/")})
    private Deepl deepl = new Deepl();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/discord/")})
    private Discord discord = new Discord();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/interactivechat/")})
    private Interactivechat interactivechat = new Interactivechat();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/itemsadder/")})
    private Itemsadder itemsadder = new Itemsadder();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/litebans/")})
    private Litebans litebans = new Litebans();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/luckperms/")})
    private Luckperms luckperms = new Luckperms();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/maintenance/")})
    private Maintenance maintenance = new Maintenance();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/minimotd/")})
    private MiniMOTD minimotd = new MiniMOTD();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/miniplaceholders/")})
    private MiniPlaceholders miniplaceholders = new MiniPlaceholders();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/motd/")})
    private MOTD motd = new MOTD();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/placeholderapi/")})
    private Placeholderapi placeholderapi = new Placeholderapi();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/plasmovoice/")})
    private Plasmovoice plasmovoice = new Plasmovoice();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/simplevoice/")})
    private Simplevoice simplevoice = new Simplevoice();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/skinsrestorer/")})
    private Skinsrestorer skinsrestorer = new Skinsrestorer();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/supervanish/")})
    private Supervanish supervanish = new Supervanish();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/tab/")})
    private TAB TAB = new TAB();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/telegram/")})
    private Telegram telegram = new Telegram();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/triton/")})
    private Triton triton = new Triton();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/twitch/")})
    private Twitch twitch = new Twitch();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/vault/")})
    private Vault vault = new Vault();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/yandex/")})
    private Yandex yandex = new Yandex();

    @Getter
    public static final class Advancedban implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
        private boolean disableFlectonepulseBan = true;
        private boolean disableFlectonepulseMute = true;
        private boolean disableFlectonepulseWarn = true;
        private boolean disableFlectonepulseKick = true;
    }

    @Getter
    public static final class Deepl implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = false;
        private String authKey = "";
    }

    @Getter
    public static final class Discord implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = false;
        private String token = "";
        private Presence presence = new Presence();
        private ChannelInfo channelInfo = new ChannelInfo();
        private Map<MessageType, String> messageChannel = new LinkedHashMap<>(){
            {
                put(MessageType.FROM_DISCORD_TO_MINECRAFT, "");
                put(MessageType.CHAT, "");
            }
        };
        private Destination destination = new Destination();

        @Getter
        public static final class Presence {
            private boolean enable = true;

            private String status = "ONLINE";
            private Activity activity = new Activity();

            @Getter
            public static final class Activity {
                private boolean enable = true;
                private String type = "PLAYING";
                private String name = "FlectonePulse";
                private String url = "https://flectone.net/pulse/";
            }
        }

        @Getter
        public static final class ChannelInfo {
            private boolean enable = false;
            private Ticker ticker = new Ticker(true, 1200);
        }
    }

    @Getter
    public static final class Interactivechat implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class Itemsadder implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class Litebans implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
        private boolean disableFlectonepulseBan = true;
        private boolean disableFlectonepulseMute = true;
        private boolean disableFlectonepulseWarn = true;
        private boolean disableFlectonepulseKick = true;
    }

    @Getter
    public static final class Luckperms implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
        private boolean tabSort = true;
    }

    @Getter
    public static final class Maintenance implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
        private boolean disableFlectonepulseMaintenance = true;
    }

    @Getter
    public static final class MiniMOTD implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
        private boolean disableFlectonepulseStatus = true;
    }

    @Getter
    public static final class MiniPlaceholders implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class MOTD implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
        private boolean disableFlectonepulseStatus = true;
    }

    @Getter
    public static final class Placeholderapi implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class Plasmovoice implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class Simplevoice implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class Skinsrestorer implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class Supervanish implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class TAB implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
        private boolean disableFlectonepulseScoreboard = true;
        private boolean disableFlectonepulseHeader = true;
        private boolean disableFlectonepulseFooter = true;
        private boolean disableFlectonepulsePlayerlistname = false;
    }

    @Getter
    public static final class Telegram implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = false;
        private String token = "";
        private Map<MessageType, List<String>> messageChannel = new LinkedHashMap<>(){
            {
                put(MessageType.FROM_TELEGRAM_TO_MINECRAFT, List.of("123456"));
                put(MessageType.CHAT, List.of("123456"));
            }
        };
        private Destination destination = new Destination();
    }

    @Getter
    public static final class Triton implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class Twitch implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = false;
        private String clientID = "";
        private String token = "";
        private Map<MessageType, List<String>> messageChannel = new LinkedHashMap<>(){
            {
                put(MessageType.FROM_TWITCH_TO_MINECRAFT, List.of("faseri4ka"));
                put(MessageType.CHAT, List.of("faseri4ka"));
            }
        };
        private Map<String, List<String>> followChannel = new LinkedHashMap<>(Map.of(
                "faseri4ka", List.of("stream start https://twitch.tv/faseri4ka")
        ));
        private Destination destination = new Destination();
    }

    @Getter
    public static final class Vault implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class Yandex implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = false;
        private String token = "";
        private String folderId = "";
    }
}
