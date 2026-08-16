package net.flectone.pulse.util.backup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.constant.DatabaseType;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.persistence.database.dump.DatabaseExporter;
import net.flectone.pulse.resolver.SystemVariableResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BackupCreatorImpl implements BackupCreator {

    private final SimpleDateFormat simpleDataFormat;
    private final SystemVariableResolver systemVariableResolver;
    private final @Named("backupPath") Path backupPath;
    private final DatabaseExporter databaseExporter;
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
        backupRows(preInitVersion, databaseName, databaseType);
    }

    private void backupRows(String preInitVersion, String databaseName, DatabaseType databaseType) {
        String backupFileName = databaseName + "_" + simpleDataFormat.format(new Date()) + ".json";
        Path backupFilePath = resolveBackupPath(preInitVersion, backupFileName);

        try {
            Files.createDirectories(backupFilePath.getParent());

            databaseExporter.export(backupFilePath);
        } catch (Exception e) {
            fLogger.warning("Failed to backup %s", e, databaseType);
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
