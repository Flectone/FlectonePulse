package net.flectone.pulse.util.backup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.constant.DatabaseType;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.resolver.SystemVariableResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BackupCreatorImpl implements BackupCreator {

    private final SimpleDateFormat simpleDataFormat;
    private final SystemVariableResolver systemVariableResolver;
    private final @Named("projectPath") Path projectPath;
    private final @Named("backupPath") Path backupPath;
    private final FLogger fLogger;

    @Override
    public void backup(String preInitVersion, Path pathToFile) {
        if (preInitVersion == null) {
            fLogger.warning("Backup is not needed if the version has not changed");
            return;
        }

        String fileName = pathToFile.getFileName().toString();
        backup(preInitVersion, fileName, pathToFile);
    }

    @Override
    public void backup(String preInitVersion, Config.Database database) {
        if (preInitVersion == null) {
            fLogger.warning("Backup is not needed if the version has not changed");
            return;
        }

        DatabaseType databaseType = database.type();
        String databaseName = systemVariableResolver.substituteEnvVars(database.name());
        switch (databaseType) {
            case SQLITE, H2 -> {
                databaseName = databaseName + (databaseType == DatabaseType.SQLITE ? ".db" : ".h2.mv.db");

                backup(preInitVersion, databaseName, projectPath.resolve(databaseName));
            }
            case MYSQL, MARIADB, POSTGRESQL -> {
                try {
                    String backupFileName = databaseName + "_" + simpleDataFormat.format(new Date()) + ".sql";
                    Path backupPath = resolveBackupPath(preInitVersion, backupFileName);

                    String host = systemVariableResolver.substituteEnvVars(database.host());
                    String port = systemVariableResolver.substituteEnvVars(database.port());
                    String user = systemVariableResolver.substituteEnvVars(database.user());
                    String password = systemVariableResolver.substituteEnvVars(database.password());

                    Map<String, String> env = new LinkedHashMap<>(System.getenv());
                    ProcessBuilder processBuilder;
                    if (databaseType == DatabaseType.MYSQL || databaseType == DatabaseType.MARIADB) {
                        env.put("MYSQL_PWD", password);
                        processBuilder = new ProcessBuilder(
                                getMySQLDumpCommand(),
                                "-h", host,
                                "-P", port,
                                "-u", user,
                                "--ssl=0",
                                "--ssl-verify-server-cert=0",
                                "--single-transaction",
                                "--routines",
                                "--triggers",
                                "--events",
                                databaseName
                        );
                    } else {
                        env.put("PGPASSWORD", password);
                        processBuilder = new ProcessBuilder(
                                "pg_dump",
                                "-h", host,
                                "-p", port,
                                "-U", user,
                                "-d", databaseName,
                                "-w"
                        );
                    }

                    processBuilder.redirectOutput(backupPath.toFile());
                    processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
                    processBuilder.environment().putAll(env);

                    Process process = processBuilder.start();

                    int exitCode = process.waitFor();
                    if (exitCode != 0) {
                        fLogger.warning("%s backup failed (exit code: %s)", databaseType, exitCode);
                    }
                } catch (IOException | InterruptedException e) {
                    fLogger.warning("Failed to backup %s: %s", databaseType, e.getMessage());
                }
            }
        }
    }

    private String getMySQLDumpCommand() {
        if (isCommandAvailable("mariadb-dump")) {
            return "mariadb-dump";
        }

        return "mysqldump";
    }

    private boolean isCommandAvailable(String command) {
        try {
            Process process = new ProcessBuilder(command, "--version").start();
            return process.waitFor() == 0;
        } catch (Exception _) {
            return false;
        }
    }

    private Path resolveBackupPath(String preInitVersion, String newFileName) {
        return backupPath.resolve(preInitVersion).resolve(newFileName);
    }

    private void backup(String preInitVersion, String fileName, Path pathToFile) {
        String newFileName = fileName + "_" + simpleDataFormat.format(new Date());
        Path backupFilePath = resolveBackupPath(preInitVersion, newFileName);

        try {
            Files.createDirectories(backupFilePath.getParent());
            Files.copy(pathToFile, backupFilePath);
        } catch (IOException e) {
            fLogger.warning("Failed to backup %s", e, fileName);
        }
    }
}
