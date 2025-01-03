package net.flectone.pulse.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.flectone.pulse.BuildConfig;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

    public Config(Path pluginPath) {
        super(Paths.get(pluginPath + File.separator + "config.yml"));
    }

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/en/config/")})
    private String console = "Console";
    @Setter
    private String version = BuildConfig.PROJECT_VERSION;
    @Setter
    private String language = "en_us";
    private boolean languagePlayer = true;
    private boolean metrics = true;
    private boolean bungeecord = false;
    private boolean velocity = false;

    private List<String> logFilter = new ArrayList<>(List.of(
            "Paper Async Command Builder",
            "Caught previously unhandled exception :",
            "Error received from Telegram GetUpdates Request",
            "OkHttp TaskRunner"
    ));

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/en/config#database")})
    private Database database = new Database();

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/en/config#module")})
    private Module module = new Module();

    @Getter
    public static final class Database {
        private net.flectone.pulse.database.Database.Type type = net.flectone.pulse.database.Database.Type.SQLITE;
        private String name = "flectonepulse";
        private String host = "localhost";
        private String port = "3306";
        private String user = "root";
        private String password = "1234";
        public Database() {}
    }

    @Getter
    public static final class Module implements IModule, IEnable {

        private boolean enable = true;

        @Override
        public ICommand getCommand() {
            return null;
        }

        @Override
        public IIntegration getIntegration() {
            return null;
        }

        @Override
        public IMessage getMessage() {
            return null;
        }
    }

    public interface IEnable {
        boolean isEnable();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Ticker implements IEnable {
        private boolean enable = true;
        private int period = -1;

        public Ticker(int period) {
            this.period = period;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Cooldown implements IEnable {
        private boolean enable = false;
        private int duration = 60;
    }
}
