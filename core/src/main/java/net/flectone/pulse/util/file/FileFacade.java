package net.flectone.pulse.util.file;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.*;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.file.FilePack;
import net.flectone.pulse.util.comparator.VersionComparator;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.creator.BackupCreator;

import java.io.IOException;
import java.util.Map;
import java.util.function.UnaryOperator;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FileFacade {

    private final FileLoader fileLoader;
    private final FileWriter fileWriter;
    private final FileMigrator fileMigrator;
    private final FilePathProvider filePathProvider;
    private final BackupCreator backupCreator;
    private final VersionComparator versionComparator;

    @Getter
    private String preInitVersion;
    private FilePack files;
    private Localization defaultLocalization;


    public void reload() throws IOException {
        fileLoader.init();

        // this is to check FlectonePulse version
        // maybe in the future we should put version in a separate file, but I think it's not so important
        preInitVersion = fileLoader.loadAndMergeConfig(files).version();
        boolean versionChanged = !preInitVersion.equals(BuildConfig.PROJECT_VERSION);

        // backup if version changed
        if (versionChanged) {
            backupFiles(preInitVersion);
        }

        // load local files
        updateFiles();

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

    public Command command() {
        return files.command();
    }

    public Config config() {
        return files.config();
    }

    public Integration integration() {
        return files.integration();
    }

    public Message message() {
        return files.message();
    }

    public Permission permission() {
        return files.permission();
    }

    public Map<String, Localization> localizations() {
        return files.localizations();
    }

    public Localization localization() {
        return localization(FPlayer.UNKNOWN);
    }

    public Localization localization(FEntity sender) {
        if (!config().language().byPlayer()) return defaultLocalization;
        if (!(sender instanceof FPlayer fPlayer)) return defaultLocalization;

        String locale = fPlayer.getSetting(SettingText.LOCALE);
        if (locale == null) return defaultLocalization;

        return localizations().getOrDefault(locale, defaultLocalization);
    }

    public void saveFiles() {
        fileWriter.save(files, false);
    }

    public void updateFiles() {
        files = fileLoader.loadFiles(files);

        defaultLocalization = files.localizations().get(files.config().language().type());
    }

    public void updateFilePack(UnaryOperator<FilePack> filePackOperator) {
        files = filePackOperator.apply(files);
    }

    private void backupFiles(String preInitVersion) {
        backupCreator.setPreInitVersion(preInitVersion);

        FilePack defaultFiles = fileLoader.getDefaultFiles();

        // we can't backup config.yml because it has already been reloaded
        backupCreator.backup(filePathProvider.get(defaultFiles.command()));
        backupCreator.backup(filePathProvider.get(defaultFiles.integration()));
        backupCreator.backup(filePathProvider.get(defaultFiles.message()));
        backupCreator.backup(filePathProvider.get(defaultFiles.permission()));

        for (Localization localization : defaultFiles.localizations().values()) {
            backupCreator.backup(filePathProvider.get(localization));
        }
    }

    private void migrateFiles(String preInitVersion) {
        // fix update permission name
        if (versionComparator.isOlderThan(preInitVersion, "1.4.3")) {
            files = fileMigrator.migration_1_4_3(files);
        }

        if (versionComparator.isOlderThan(preInitVersion, "1.5.0")) {
            files = fileMigrator.migration_1_5_0(files);
        }

        if (versionComparator.isOlderThan(preInitVersion, "1.6.0")) {
            files = fileMigrator.migration_1_6_0(files);
        }

        if (versionComparator.isOlderThan(preInitVersion, "1.7.0")) {
            files = fileMigrator.migration_1_7_0(files);
        }

        if (versionComparator.isOlderThan(preInitVersion, "1.7.1")) {
            files = fileMigrator.migration_1_7_1(files);
        }

        if (versionComparator.isOlderThan(preInitVersion, "1.7.2")) {
            files = fileMigrator.migration_1_7_2(files);
        }

        if (versionComparator.isOlderThan(preInitVersion, "1.7.4")) {
            files = fileMigrator.migration_1_7_4(files);
        }

        if (versionComparator.isOlderThan(preInitVersion, "1.7.5")) {
            files = fileMigrator.migration_1_7_5(files);
        }

        files = files.withConfig(files.config().withVersion(BuildConfig.PROJECT_VERSION));
    }
}
