package net.flectone.pulse.util.creator;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.Setter;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.config.YamlFile;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.util.logging.FLogger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class BackupCreator {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    private final SystemVariableResolver systemVariableResolver;
    private final Path projectPath;
    private final Path backupPath;
    private final FLogger fLogger;

    @Setter
    @Nullable
    private String preInitVersion;

    @Inject
    public BackupCreator(SystemVariableResolver systemVariableResolver,
                         @Named("projectPath") Path projectPath,
                         FLogger fLogger) {
        this.systemVariableResolver = systemVariableResolver;
        this.projectPath = projectPath;
        this.backupPath = projectPath.resolve("backups");
        this.fLogger = fLogger;
    }

    public <T extends YamlFile> void backup(T yamlFile) {
        if (preInitVersion == null) {
            fLogger.warning("Backup is not needed if the version has not changed");
            return;
        }

        String fileName = yamlFile.getPathToFile().getFileName().toString();
        backup(fileName, yamlFile.getPathToFile());
    }

    public void backup(Config.Database database) {
        if (preInitVersion == null) {
            fLogger.warning("Backup is not needed if the version has not changed");
            return;
        }

        Database.Type databaseType = database.getType();
        String databaseName = systemVariableResolver.substituteEnvVars(database.getName());
        switch (databaseType) {
            case SQLITE, H2 -> {
                databaseName = databaseName + (databaseType == Database.Type.SQLITE ? ".db" : ".h2.mv.db");

                backup(databaseName, projectPath.resolve(databaseName));
            }
            case MYSQL, POSTGRESQL -> {
                try {
                    String backupFileName = databaseName + "_" + simpleDateFormat.format(new Date()) + ".sql";
                    Path backupPath = resolveBackupPath(backupFileName);

                    String host = systemVariableResolver.substituteEnvVars(database.getHost());
                    String port = systemVariableResolver.substituteEnvVars(database.getPort());
                    String user = systemVariableResolver.substituteEnvVars(database.getUser());
                    String password = systemVariableResolver.substituteEnvVars(database.getPassword());

                    Map<String, String> env = new HashMap<>(System.getenv());
                    ProcessBuilder processBuilder;
                    if (databaseType == Database.Type.MYSQL) {
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
                        fLogger.warning(databaseType + " backup failed (exit code: " + exitCode + ")");
                    }
                } catch (IOException | InterruptedException e) {
                    fLogger.warning("Failed to backup " + databaseType + ": " + e.getMessage());
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
        } catch (Exception e) {
            return false;
        }
        }

    private Path resolveBackupPath(String newFileName) {
        return backupPath.resolve(preInitVersion).resolve(newFileName);
    }

    private void backup(String fileName, Path pathToFile) {
        String newFileName = fileName + "_" + simpleDateFormat.format(new Date());
        Path backupFilePath = resolveBackupPath(newFileName);

        try {
            Files.createDirectories(backupFilePath.getParent());
            Files.copy(pathToFile, backupFilePath);
        } catch (IOException e) {
            fLogger.warning("Failed to backup " + fileName, e);
        }
    }
}
