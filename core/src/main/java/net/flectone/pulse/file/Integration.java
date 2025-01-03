package net.flectone.pulse.file;

import lombok.Getter;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.flectone.pulse.util.MessageTag;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public Integration(Path pluginPath) {
        super(Paths.get(pluginPath + File.separator + "integration.yml"));
    }

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/en/integration/")})
    private boolean enable = true;

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/en/integration/discord/")})
    private Discord discord = new Discord();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/en/integration/interactivechat/")})
    private Interactivechat interactivechat = new Interactivechat();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/en/integration/luckperms/")})
    private Luckperms luckperms = new Luckperms();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/en/integration/placeholderapi/")})
    private Placeholderapi placeholderapi = new Placeholderapi();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/en/integration/plasmovoice/")})
    private Plasmovoice plasmovoice = new Plasmovoice();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/en/integration/simplevoice/")})
    private Simplevoice simplevoice = new Simplevoice();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/en/integration/skinsrestorer/")})
    private Skinsrestorer skinsrestorer = new Skinsrestorer();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/en/integration/supervanish/")})
    private Supervanish supervanish = new Supervanish();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/en/integration/telegram/")})
    private Telegram telegram = new Telegram();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/en/integration/twitch/")})
    private Twitch twitch = new Twitch();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/en/integration/vault/")})
    private Vault vault = new Vault();

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
            private Config.Ticker ticker = new Config.Ticker(1200);
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
    }

    @Getter
    public static final class Vault implements ISubIntegration, Config.IEnable {
        private boolean enable = true;
    }
}
