package net.flectone.pulse.file;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.*;
import net.flectone.pulse.model.file.FilePack;
import net.flectone.pulse.util.backup.BackupCreator;
import net.flectone.pulse.util.version.VersionComparator;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

@Singleton
public class FileFacadeImpl implements FileFacade {

    private final FileLoader fileLoader;
    private final FileWriter fileWriter;
    private final FileMigrator fileMigrator;
    private final FilePathProvider filePathProvider;
    private final BackupCreator backupCreator;
    private final VersionComparator versionComparator;

    private String preInitVersion;
    private FilePack files;
    private Localization defaultLocalization;

    @Inject
    public FileFacadeImpl(FileLoader fileLoader,
                      FileWriter fileWriter,
                      FileMigrator fileMigrator,
                      FilePathProvider filePathProvider,
                      BackupCreator backupCreator,
                      VersionComparator versionComparator) throws IOException {
        this.fileLoader = fileLoader;
        this.fileWriter = fileWriter;
        this.fileMigrator = fileMigrator;
        this.filePathProvider = filePathProvider;
        this.backupCreator = backupCreator;
        this.versionComparator = versionComparator;

        reload();
    }

    @Override
    public void reload() throws IOException {
        fileLoader.init();

        // load FlectonePulse version
        preInitVersion = fileLoader.loadVersion();

        // check version
        boolean versionChanged = !preInitVersion.equals(BuildConfig.PROJECT_VERSION);

        // backup if version changed
        if (versionChanged) {
            backupFiles(preInitVersion);
        }

        // load local files
        updateFiles();

        boolean serverEmpty = StringUtils.isEmpty(files.config().server());
        if (serverEmpty) {
            files = files.withConfig(files.config().withServer(UUID.randomUUID().toString()));
        }

        // migrate if version changed
        if (versionChanged) {
            migrateFiles(preInitVersion);
        }

        saveFiles();

        // fix migration problems
        if (versionChanged) {
            updateFiles();
        }
    }

    @Override
    public Command command() {
        return files.command();
    }

    @Override
    public Config config() {
        return files.config();
    }

    @Override
    public Integration integration() {
        return files.integration();
    }

    @Override
    public Message message() {
        return files.message();
    }

    @Override
    public Permission permission() {
        return files.permission();
    }

    @Override
    public Map<String, Localization> localizations() {
        return files.localizations();
    }

    @Override
    public Localization localization() {
        return localization(null);
    }

    @Override
    public Localization localization(@Nullable String locale) {
        if (!config().language().byPlayer()) return defaultLocalization;
        if (locale == null) return defaultLocalization;

        return localizations().getOrDefault(locale, defaultLocalization);
    }

    @Override
    public void saveFiles() {
        fileWriter.save(files, false, false);
    }

    @Override
    public void updateFiles() {
        files = fileLoader.loadFiles(files);

        defaultLocalization = files.localizations().get(files.config().language().type());
    }

    @Override
    public void updateFilePack(UnaryOperator<FilePack> filePackOperator) {
        files = filePackOperator.apply(files);
    }

    @Override
    public String getPreInitVersion() {
        return preInitVersion;
    }

    private void backupFiles(String preInitVersion) {
        FilePack defaultFiles = fileLoader.getDefaultFiles();

        // we can't backup config.yml because it has already been reloaded
        backupCreator.backup(preInitVersion, filePathProvider.get(defaultFiles.command()));
        backupCreator.backup(preInitVersion, filePathProvider.get(defaultFiles.integration()));
        backupCreator.backup(preInitVersion, filePathProvider.get(defaultFiles.message()));
        backupCreator.backup(preInitVersion, filePathProvider.get(defaultFiles.permission()));

        for (Localization localization : defaultFiles.localizations().values()) {
            backupCreator.backup(preInitVersion, filePathProvider.get(localization));
        }
    }

    private void migrateFiles(String preInitVersion) {
        if (versionComparator.isOlderThan(preInitVersion, "1.11.1")) { // 1.11.1 == 1.12.0
            files = fileMigrator.migration_1_11_1(files);
        }

        if (versionComparator.isOlderThan(preInitVersion, "1.12.3")) { // 1.12.3 == 1.13.0
            files = fileMigrator.migration_1_12_3(files);
        }

        files = files.withConfig(files.config().withVersion(BuildConfig.PROJECT_VERSION));
    }
}
