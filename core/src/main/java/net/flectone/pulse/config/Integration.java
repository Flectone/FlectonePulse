package net.flectone.pulse.config;

import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.OptBoolean;
import lombok.Getter;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.util.constant.MessageType;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"FieldMayBeFinal", "unused"})
@Getter
public final class Integration extends YamlFile implements ModuleConfig.IntegrationConfig, Config.IEnable {

    public Integration(Path projectPath) {
        super(projectPath.resolve("integration.yml"));
    }

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/")
    private boolean enable = true;

    private String avatarApiUrl = "https://mc-heads.net/avatar/<skin>/8.png";
    private String bodyApiUrl = "https://mc-heads.net/player/<skin>/16.png";

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/advancedban/")
    private Advancedban advancedban = new Advancedban();

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/advancedban/") // Ссылку на свои доксы вставишь
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
    public static final class Advancedban implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
        private boolean disableFlectonepulseBan = true;
        private boolean disableFlectonepulseMute = true;
        private boolean disableFlectonepulseWarn = true;
        private boolean disableFlectonepulseKick = true;
    }

    @Getter
    public static final class Libertybans implements SubIntegrationConfig, Config.IEnable {
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

        @JsonMerge(OptBoolean.FALSE)
        private Map<String, String> messageChannel = new LinkedHashMap<>(){
            {
                put(MessageType.FROM_DISCORD_TO_MINECRAFT.name(), "");
                put("CHAT_GLOBAL", "");
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
    }

    @Getter
    public static final class ChannelInfo {
        private boolean enable = false;
        private Ticker ticker = new Ticker(true, 1200);
    }

    @Getter
    public static final class Geyser implements SubIntegrationConfig, Config.IEnable {
        private boolean enable = true;
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

        private ChannelInfo channelInfo = new ChannelInfo();

        @JsonMerge(OptBoolean.FALSE)
        private Map<String, List<String>> messageChannel = new LinkedHashMap<>(){
            {
                put(MessageType.FROM_TELEGRAM_TO_MINECRAFT.name(), List.of("123456"));
                put("CHAT_GLOBAL", List.of("123456"));
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

        @JsonMerge(OptBoolean.FALSE)
        private Map<String, List<String>> messageChannel = new LinkedHashMap<>(){
            {
                put(MessageType.FROM_TWITCH_TO_MINECRAFT.name(), List.of("faseri4ka"));
                put("CHAT_GLOBAL", List.of("faseri4ka"));
            }
        };

        @JsonMerge(OptBoolean.FALSE)
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
