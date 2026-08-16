package net.flectone.pulse.persistence.database.migration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.exception.DatabaseMigrationException;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.util.loader.DriverLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class H2StoreMigrator {

    private static final int STORE_HEADER_LENGTH = 256;
    private static final int UNKNOWN_STORE_FORMAT = -1;

    // mvstore keeps its header as plain text at the start of the file
    private static final Pattern STORE_FORMAT_PATTERN = Pattern.compile("format:(\\d{1,9})");

    // a dump names constraints the way the h2 writing it did, and those names can collide with
    // the ones the current h2 generates while running the dump, so it may name them itself
    private static final Pattern GENERATED_CONSTRAINT_PATTERN = Pattern.compile("ADD CONSTRAINT \"[^\"]+\"\\.\"CONSTRAINT_[^\"]*\" ");

    private static final Map<Integer, String> LEGACY_VERSIONS = Map.of(
            1, "1.4.200",
            2, "2.1.214"
    );

    private final @Named("backupPath") Path backupPath;
    private final SimpleDateFormat simpleDataFormat;
    private final DriverLoader driverLoader;
    private final FLogger fLogger;

    public boolean migrate(Path databaseFile, String connectionURL, Driver driver) {
        int storeFormat = readStoreFormat(databaseFile);

        String legacyVersion = LEGACY_VERSIONS.get(storeFormat);
        if (legacyVersion == null) return false;

        Path dumpFile = databaseFile.resolveSibling(databaseFile.getFileName() + ".migration.sql");

        try {
            export(driverLoader.loadLegacyH2(legacyVersion), connectionURL, dumpFile);
            rewriteGeneratedConstraints(dumpFile);
        } catch (Exception e) {
            deleteQuietly(dumpFile);

            throw new DatabaseMigrationException("Failed to dump " + databaseFile + " with h2 " + legacyVersion, e);
        }

        Path backupFile = moveToBackup(databaseFile, storeFormat);

        try {
            runScript(driver, connectionURL, dumpFile);
        } catch (Exception e) {
            restore(databaseFile, backupFile);

            throw new DatabaseMigrationException("Failed to load " + dumpFile + " with h2 " + BuildConfig.H2_VERSION + ", " + databaseFile + " is left as it was", e);
        }

        deleteQuietly(dumpFile);

        fLogger.warning("Database migrated to h2 store format %s, the old file is kept as %s", BuildConfig.H2_VERSION, backupFile);

        return true;
    }

    private int readStoreFormat(Path databaseFile) {
        if (!Files.isRegularFile(databaseFile)) return UNKNOWN_STORE_FORMAT;

        byte[] header = new byte[STORE_HEADER_LENGTH];
        int length;
        try (InputStream inputStream = Files.newInputStream(databaseFile)) {
            length = inputStream.readNBytes(header, 0, header.length);
        } catch (IOException e) {
            fLogger.warning("Failed to read the header of %s", e, databaseFile);

            return UNKNOWN_STORE_FORMAT;
        }

        Matcher matcher = STORE_FORMAT_PATTERN.matcher(new String(header, 0, length, StandardCharsets.ISO_8859_1));
        if (!matcher.find()) return UNKNOWN_STORE_FORMAT;

        return Integer.parseInt(matcher.group(1));
    }

    private void export(Driver legacyDriver, String connectionURL, Path dumpFile) throws SQLException {
        execute(legacyDriver, connectionURL, "SCRIPT TO '" + toSQLLiteral(dumpFile) + "' CHARSET 'UTF-8'");
    }

    private void runScript(Driver driver, String connectionURL, Path dumpFile) throws SQLException {
        execute(driver, connectionURL, "RUNSCRIPT FROM '" + toSQLLiteral(dumpFile) + "' CHARSET 'UTF-8'");
    }

    private void execute(Driver driver, String connectionURL, String sql) throws SQLException {
        try (Connection connection = connect(driver, connectionURL);
             Statement statement = connection.createStatement()) {
            try {
                statement.execute(sql);
            } finally {
                shutdown(statement);
            }
        }
    }

    private void shutdown(Statement statement) {
        try {
            statement.execute("SHUTDOWN");
        } catch (SQLException e) {
            fLogger.warning("Failed to close the database after a migration statement", e);
        }
    }

    private Connection connect(Driver driver, String connectionURL) throws SQLException {
        Connection connection = driver.connect(connectionURL, new Properties());
        if (connection == null) {
            throw new SQLException("Driver did not accept " + connectionURL);
        }

        return connection;
    }

    private void rewriteGeneratedConstraints(Path dumpFile) throws IOException {
        Path rewrittenFile = dumpFile.resolveSibling(dumpFile.getFileName() + ".tmp");

        try (BufferedReader reader = Files.newBufferedReader(dumpFile, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(rewrittenFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(GENERATED_CONSTRAINT_PATTERN.matcher(line).replaceAll("ADD "));
                writer.newLine();
            }
        }

        Files.move(rewrittenFile, dumpFile, StandardCopyOption.REPLACE_EXISTING);
    }

    private Path moveToBackup(Path databaseFile, int storeFormat) {
        Path backupFile = backupPath
                .resolve("h2-store-format-" + storeFormat)
                .resolve(databaseFile.getFileName() + "_" + simpleDataFormat.format(new Date()));

        try {
            Files.createDirectories(backupFile.getParent());
            Files.move(databaseFile, backupFile);
        } catch (IOException e) {
            throw new DatabaseMigrationException("Failed to move " + databaseFile + " to " + backupFile, e);
        }

        return backupFile;
    }

    private void restore(Path databaseFile, Path backupFile) {
        try {
            Files.deleteIfExists(databaseFile);
            Files.move(backupFile, databaseFile);
        } catch (IOException e) {
            fLogger.warning("Failed to put %s back, the database is left at %s", e, databaseFile, backupFile);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            fLogger.warning("Failed to delete %s", e, path);
        }
    }

    private String toSQLLiteral(Path path) {
        return path.toAbsolutePath().toString().replace("'", "''");
    }

}
