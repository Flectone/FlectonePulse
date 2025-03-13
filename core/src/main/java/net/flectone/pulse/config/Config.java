package net.flectone.pulse.config;

import lombok.Getter;
import lombok.Setter;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.flectone.pulse.BuildConfig;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
public final class Config extends FileSerializable {

    public Config(Path projectPath) {
        super(projectPath.resolve("config.yml"));
    }

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/config/")})
    private String console = "Console";
    @Setter
    private String version = BuildConfig.PROJECT_VERSION;
    @Setter
    private String language = "en_us";
    private boolean languagePlayer = true;
    private boolean metrics = true;
    private boolean bungeecord = false;
    private boolean velocity = false;
    private Set<String> clusters = new HashSet<>();

    private List<String> logFilter = new ArrayList<>(List.of(
            "Paper Async Command Builder",
            "Caught previously unhandled exception :",
            "Error received from Telegram GetUpdates Request",
            "OkHttp TaskRunner",
            "Exception in thread \"ForkJoinPool"
    ));

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/config#database")})
    private Database database = new Database();

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/config#module")})
    private Module module = new Module();

    @Getter
    public static final class Database {
        private Type type = Type.SQLITE;
        private String name = "flectonepulse";
        private String host = "localhost";
        private String port = "3306";
        private String user = "root";
        private String password = "1234";
        private String parameters = "?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8";
        public Database() {}

        public enum Type {
            SQLITE,
            MYSQL
        }
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

    public interface IEnable {
        boolean isEnable();
    }
}
