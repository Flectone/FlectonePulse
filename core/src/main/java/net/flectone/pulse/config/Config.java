package net.flectone.pulse.config;

import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.OptBoolean;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.util.constant.CacheName;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"FieldMayBeFinal", "unused"})
@Getter
public final class Config extends YamlFile {

    public Config(Path projectPath) {
        super(projectPath.resolve("config.yml"));
    }

    @Setter
    @JsonPropertyDescription(" Don't change it if you don't know what it is")
    private String version = BuildConfig.PROJECT_VERSION;

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/language/")
    private Language language = new Language();

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/database/")
    private Database database = new Database();

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/proxy/")
    private Proxy proxy = new Proxy();

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/command/")
    private Command command = new Command();

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/module/")
    private Module module = new Module();

    @JsonPropertyDescription("https://flectone.net/pulse/docs/config/editor/")
    private Editor editor = new Editor();

    @JsonPropertyDescription("https://flectone.net/pulse/docs/config/logger/")
    private Logger logger = new Logger();

    @JsonPropertyDescription("https://flectone.net/pulse/docs/config/cache/")
    private Cache cache = new Cache();

    @JsonPropertyDescription("Help us improve FlectonePulse! This collects basic, anonymous data like server version and module usage. \nNo personal data, No IPs, No player names. \nThis helps us understand what features matter most and focus development where it's needed. \nYou can see the public stats here: https://flectone.net/pulse/metrics/ \nThanks for supporting the project! ❤️")
    private Metrics metrics = new Metrics();

    @Getter
    public static final class Language {

        @Setter
        private String type = "en_us";

        private boolean byPlayer = true;

    }

    @Getter
    @Setter
    public static final class Database {
        private net.flectone.pulse.data.database.Database.Type type = net.flectone.pulse.data.database.Database.Type.H2;
        private String name = "flectonepulse";
        private String host = "127.0.0.1";
        private String port = "3306";
        private String user = "root";
        private String password = "1234";
        private String parameters = "?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8";
        private String prefix = "";
        public Database() {}
    }

    @Getter
    public static final class Proxy {

        @JsonMerge(OptBoolean.FALSE)
        private Set<String> clusters = new HashSet<>();

        private boolean bungeecord = false;

        private boolean velocity = false;

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/proxy#redis")
        private Redis redis = new Redis();

        @Getter
        public static final class Redis implements IEnable {
            private boolean enable = false;
            private String host = "127.0.0.1";
            private int port = 6379;
            private boolean ssl = false;
            private String user = "";
            private String password = "";
            public Redis() {}
        }
    }

    @Getter
    public static final class Command {

        private boolean unregisterOnReload = false;

        @JsonMerge(OptBoolean.FALSE)
        private Set<String> disabledFabric = new HashSet<>(Set.of(
                "tell", "msg", "w", "me", "ban", "kick", "pardon", "banlist"
        ));

    }

    @Getter
    public static final class Module implements ModuleConfig, IEnable {

        private boolean enable = true;
        private boolean usePaperMessageSender = false;

        @Override
        public CommandConfig getCommand() {
            return null;
        }

        @Override
        public IntegrationConfig getIntegration() {
            return null;
        }

        @Override
        public MessageConfig getMessage() {
            return null;
        }
    }

    @Getter
    public static final class Editor {
        private String host = "";
        private boolean https = false;
        private int port = 25566;
    }

    @Getter
    public static final class Logger {
        private String console = "Console";
        private String prefix = "\033[0;34m(FlectonePulse) \033[0m";

        @JsonMerge(OptBoolean.FALSE)
        private List<String> description = new ArrayList<>(List.of(
                " \033[0;34m ___       ___  __  ___  __        ___ \033[0m",
                " \033[0;34m|__  |    |__  /  `  |  /  \\ |\\ | |__  \033[0m",
                " \033[0;34m|    |___ |___ \\__,  |  \\__/ | \\| |___ \033[0m",
                " \033[0;34m __             __   ___ \033[0;96m                       \033[0m",
                " \033[0;34m|__) |  | |    /__` |__  \033[0;96m                   \033[0m",
                " \033[0;34m|    \\__/ |___ .__/ |___\033[0;96m   /\\         \033[0m",
                " \033[0;96m                          /  \\ v<version>               \033[0m",
                " \033[0;96m__/\\___  ____/\\_____  ___/    \\______ \033[0m",
                " \033[0;96m       \\/           \\/  \033[0m"
        ));

        private String warn = "\033[0;93m";
        private String info = "\033[0;96m";

        @JsonMerge(OptBoolean.FALSE)
        private List<String> filter = new ArrayList<>(List.of(
                "Paper Async Command Builder",
                "Caught previously unhandled exception :",
                "Error received from Telegram GetUpdates Request",
                "OkHttp TaskRunner",
                "Exception in thread \"ForkJoinPool",
                "FlectonePulseDatabase - "
        ));
    }

    @Getter
    public static final class Cache {

        @JsonMerge(OptBoolean.FALSE)
        private Map<CacheName, CacheSetting> types = new EnumMap<>(CacheName.class){{
            put(CacheName.DIALOG_CLICK, new CacheSetting(1, TimeUnit.SECONDS, 100));
            put(CacheName.OFFLINE_PLAYERS, new CacheSetting(1, TimeUnit.HOURS, 1000));
            put(CacheName.MODERATION, new CacheSetting(1, TimeUnit.HOURS, 5000));
            put(CacheName.LEGACY_COLOR_MESSAGE, new CacheSetting(10, TimeUnit.MINUTES, 100000));
            put(CacheName.MENTION_MESSAGE, new CacheSetting(10, TimeUnit.MINUTES, 1000));
            put(CacheName.SWEAR_MESSAGE, new CacheSetting(10, TimeUnit.MINUTES, 100000));
            put(CacheName.REPLACEMENT_MESSAGE, new CacheSetting(10, TimeUnit.MINUTES, 100000));
            put(CacheName.REPLACEMENT_IMAGE, new CacheSetting(10, TimeUnit.MINUTES, 100));
            put(CacheName.TRANSLATE_MESSAGE, new CacheSetting(1, TimeUnit.HOURS, 5000));
            put(CacheName.PROFILE_PROPERTY, new CacheSetting(1, TimeUnit.HOURS, 1000));
        }};

        @Getter
        @AllArgsConstructor
        @NoArgsConstructor
        public static final class CacheSetting {
            private long duration = 1;
            private TimeUnit timeUnit = TimeUnit.MINUTES;
            private long size = 100;
        }

    }

    @Getter
    public static final class Metrics {
        private boolean enable = true;
    }

    public interface IEnable {
        boolean isEnable();
    }
}
