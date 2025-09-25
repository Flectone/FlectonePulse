package net.flectone.pulse.config;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.BuildConfig;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"FieldMayBeFinal", "unused"})
@Getter
public final class Config extends YamlFile {

    public Config(Path projectPath) {
        super(projectPath.resolve("config.yml"));
    }

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/")
    private String console = "Console";

    @Setter
    private String version = BuildConfig.PROJECT_VERSION;

    @Setter
    private String language = "en_us";

    private boolean languagePlayer = true;

    private boolean unregisterOwnCommands = false;

    private boolean metrics = true;

    private boolean bungeecord = false;

    private boolean velocity = false;

    private Set<String> clusters = new HashSet<>();

    private Set<String> fabricDisabledCommands = new HashSet<>(Set.of(
            "tell", "msg", "w", "me", "ban", "kick", "pardon", "banlist"
    ));

    private List<String> logFilter = new ArrayList<>(List.of(
            "Paper Async Command Builder",
            "Caught previously unhandled exception :",
            "Error received from Telegram GetUpdates Request",
            "OkHttp TaskRunner",
            "Exception in thread \"ForkJoinPool",
            "FlectonePulseDatabase - "
    ));

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/#database")
    private Database database = new Database();

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/#module")
    private Module module = new Module();

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/#redis")
    private Redis redis = new Redis();

    @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/#editor")
    private Editor editor = new Editor();

    @Getter
    @Setter
    public static final class Database {
        private net.flectone.pulse.data.database.Database.Type type = net.flectone.pulse.data.database.Database.Type.SQLITE;
        private String name = "flectonepulse";
        private String host = "127.0.0.1";
        private String port = "3306";
        private String user = "root";
        private String password = "1234";
        private String parameters = "?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8";
        public Database() {}
    }

    @Getter
    public static final class Module implements ModuleConfig, IEnable {

        private boolean enable = true;

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
    public static final class Redis implements IEnable {
        private boolean enable = false;
        private String host = "127.0.0.1";
        private int port = 6379;
        private boolean ssl = false;
        private String user = "";
        private String password = "";
        public Redis() {}
    }

    @Getter
    public static final class Editor {
        private String host = "";
        private boolean https = false;
        private int port = 25566;
    }

    public interface IEnable {
        boolean isEnable();
    }
}
