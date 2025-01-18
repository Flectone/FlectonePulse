package net.flectone.pulse.database.sqlite;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.file.Config;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.util.SystemUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Singleton
public class SQLiteDatabase extends Database {

    private final Config.Database config;

    private final Path projectPath;
    private final InputStream SQLFile;
    private final SystemUtil systemUtil;
    private final FLogger fLogger;

    private String connectionURL;

    @Inject
    public SQLiteDatabase(FileManager fileManager,
                          @Named("projectPath") Path projectPath,
                          @Named("SQLFile") InputStream SQLFile,
                          SystemUtil systemUtil,
                          FLogger fLogger) {
        this.projectPath = projectPath;
        this.SQLFile = SQLFile;
        this.systemUtil = systemUtil;
        this.fLogger = fLogger;

        config = fileManager.getConfig().getDatabase();

        SQL_INSERT_OR_IGNORE_FPLAYER = "INSERT OR IGNORE INTO `player` (`id`, `uuid`, `name`) VALUES (?, ?, ?)";
        SQL_INSERT_OR_UPDATE_COLOR = "INSERT INTO `color` (`name`) " +
                "VALUES (?) " +
                "ON CONFLICT(`name`) DO UPDATE SET " +
                "`name` = excluded.`name`";
        SQL_INSERT_OR_UPDATE_PLAYER_COLOR = "INSERT OR REPLACE INTO `player_color` (`number`, `player`, `color`) " +
                "VALUES (?,?,?) ";
    }

    @Override
    public void connect() throws SQLException, IOException {
        connectionURL = new StringBuilder()
                .append("jdbc:sqlite:")
                .append(projectPath.toString())
                .append(File.separator)
                .append(systemUtil.substituteEnvVars(config.getName()))
                .append(".db")
                .toString();

        try(Connection connection = DriverManager.getConnection(connectionURL)) {
            if (connection != null) {
                executeFile(SQLFile);
                init();

                fLogger.info("SQLite Database connected");
            }
        }
    }

    @NotNull
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionURL);
    }

    @Override
    public void init() throws SQLException {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("pragma journal_mode = WAL;");
            statement.execute("pragma synchronous = normal;");
            statement.execute("pragma journal_size_limit = 6144000;");
        }
    }

    @Override
    public void disconnect() {
        fLogger.info("SQLite Database disconnected");
    }
}
