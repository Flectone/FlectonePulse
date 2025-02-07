package net.flectone.pulse.file;

import lombok.Getter;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.flectone.pulse.model.Destination;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.util.MessageTag;

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
public final class Integration extends FileSerializable implements IModule.IIntegration {

    public Integration(Path projectPath) {
        super(projectPath.resolve("integration.yml"));
    }

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/")})
    private boolean enable = true;

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/deepl/")})
    private Deepl deepl = new Deepl();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/discord/")})
    private Discord discord = new Discord();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/interactivechat/")})
    private Interactivechat interactivechat = new Interactivechat();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/luckperms/")})
    private Luckperms luckperms = new Luckperms();
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
    public static final class Deepl implements ISubIntegration, Config.IEnable {
        private boolean enable = false;
        private String authKey = "";
    }

    @Getter
    public static final class Discord implements ISubIntegration, Config.IEnable {
        private boolean enable = false;
        private String token = "";
        private Presence presence = new Presence();
        private ChannelInfo channelInfo = new ChannelInfo();
        private Map<MessageTag, String> messageChannel = new LinkedHashMap<>(){
            {
                put(MessageTag.FROM_DISCORD_TO_MINECRAFT, "");
                put(MessageTag.CHAT, "");
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
    public static final class Interactivechat implements ISubIntegration, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class Luckperms implements ISubIntegration, Config.IEnable {
        private boolean enable = true;
        private boolean tabSort = false;
    }

    @Getter
    public static final class Placeholderapi implements ISubIntegration, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class Plasmovoice implements ISubIntegration, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class Simplevoice implements ISubIntegration, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class Skinsrestorer implements ISubIntegration, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class Supervanish implements ISubIntegration, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class Telegram implements ISubIntegration, Config.IEnable {
        private boolean enable = false;
        private String token = "";
        private Map<MessageTag, List<String>> messageChannel = new LinkedHashMap<>(){
            {
                put(MessageTag.FROM_TELEGRAM_TO_MINECRAFT, new ArrayList<>());
                put(MessageTag.CHAT, new ArrayList<>());
            }
        };
        private Destination destination = new Destination();
    }

    @Getter
    public static final class Triton implements ISubIntegration, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class Twitch implements ISubIntegration, Config.IEnable {
        private boolean enable = false;
        private String clientID = "";
        private String token = "";
        private Map<MessageTag, List<String>> messageChannel = new LinkedHashMap<>(){
            {
                put(MessageTag.FROM_TWITCH_TO_MINECRAFT, new ArrayList<>());
                put(MessageTag.CHAT, new ArrayList<>());
            }
        };
        private Map<String, List<String>> followChannel = new LinkedHashMap<>(Map.of(
                "faseri4ka", List.of("stream start https://twitch.tv/faseri4ka")
        ));
        private Destination destination = new Destination();
    }

    @Getter
    public static final class Vault implements ISubIntegration, Config.IEnable {
        private boolean enable = true;
    }

    @Getter
    public static final class Yandex implements ISubIntegration, Config.IEnable {
        private boolean enable = false;
        private String token = "";
        private String folderId = "";
    }
}
