package net.flectone.pulse.processing.resolver;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.Getter;
import net.elytrium.serializer.language.object.AbstractSerializable;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.*;
import net.flectone.pulse.configuration.*;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.logging.FLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;


@Singleton
public class FileResolver {

    @Getter private final Map<String, Localization> localizationMap = new HashMap<>();

    @Getter private final Path projectPath;
    @Getter private final Command command;
    @Getter private final Config config;
    @Getter private final Integration integration;
    @Getter private final Message message;
    @Getter private final Permission permission;
    private final FLogger fLogger;

    @Getter private String preInitVersion;

    private Localization defaultLocalization;

    @Inject
    public FileResolver(@Named("projectPath") Path projectPath,
                        FLogger fLogger) {
        this.projectPath = projectPath;
        this.command = new Command(projectPath);
        this.config = new Config(projectPath);
        this.integration = new Integration(projectPath);
        this.message = new Message(projectPath);
        this.permission = new Permission(projectPath);
        this.fLogger = fLogger;
    }

    public Localization getLocalization() {
        return getLocalization(FPlayer.UNKNOWN);
    }

    public Localization getLocalization(FEntity sender) {
        if (!config.isLanguagePlayer()) return defaultLocalization;
        if (!(sender instanceof FPlayer fPlayer)) return defaultLocalization;

        return localizationMap.getOrDefault(fPlayer.getSettingValue(FPlayer.Setting.LOCALE), defaultLocalization);
    }

    public void reload() {
        config.reload();
        config.setLanguage(config.getLanguage());

        preInitVersion = config.getVersion();

        if (!preInitVersion.equals(BuildConfig.PROJECT_VERSION)) {
            config.setVersion(BuildConfig.PROJECT_VERSION);
            config.save();
        }

        command.reload();
        integration.reload();
        message.reload();
        permission.reload();

        reloadLanguages();

        defaultLocalization = localizationMap.get(config.getLanguage());
    }

    public void save() {
        command.save();
        config.save();
        integration.save();
        message.save();
        permission.save();
        localizationMap.values().forEach(AbstractSerializable::save);
    }

    private void reloadLanguages() {
        Set<String> newLanguages = new HashSet<>(Set.of("ru_ru", "en_us"));
        newLanguages.add(config.getLanguage());

        try (Stream<Path> paths = Files.walk(projectPath.resolve(Localization.FOLDER_NAME))) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                File localization = path.toFile();
                String localizationName = localization.getName();
                if (localizationName.endsWith(".yml")) {
                    newLanguages.add(localizationName.replace(".yml", ""));
                }
            });
        } catch (NoSuchFileException ignored) {
            // ignore first startup
        } catch (IOException e) {
            fLogger.warning(e);
        }

        newLanguages.forEach(this::loadLanguage);
    }

    private void loadLanguage(String language) {
        Localization localization = new Localization(projectPath, language);
        localization.reload(localization.getPath());
        localizationMap.put(language, localization);
    }

    public boolean isVersionOlderThan(String first, String second) {
        String[] subFirst = parseVersionNumbers(first);
        if (subFirst.length != 3) return false;

        String[] subSecond = parseVersionNumbers(second);
        if (subSecond.length != 3) return true;

        for (int i = 0; i < 3; i++) {
            int intFirst = Integer.parseInt(subFirst[i]);
            int intSecond = Integer.parseInt(subSecond[i]);

            if (intFirst < intSecond) {
                return true;
            }

            if (intFirst > intSecond) {
                return false;
            }
        }

        return false;
    }

    private String[] parseVersionNumbers(String string) {
        int endIndex = string.indexOf('-');
        return (endIndex == -1 ? string : string.substring(0, endIndex)).split("\\.");
    }
}
