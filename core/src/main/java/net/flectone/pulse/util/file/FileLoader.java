package net.flectone.pulse.util.file;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.*;
import net.flectone.pulse.config.merger.*;
import net.flectone.pulse.exception.FileLoadException;
import net.flectone.pulse.model.file.FilePack;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.util.constant.DefaultLocalization;
import net.flectone.pulse.util.constant.FilePath;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.Strings;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.MismatchedInputException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FileLoader {

    private final FileWriter fileWriter;
    private final ObjectMapper yamlMapper;
    private final @Named("projectPath") Path projectPath;
    private final FLogger fLogger;
    private final CommandMergerImpl commandMerger;
    private final ConfigMergerImpl configMerger;
    private final IntegrationMergerImpl integrationMerger;
    private final LocalizationMergerImpl localizationMerger;
    private final MessageMergerImpl messageMerger;
    private final PermissionMergerImpl permissionMerger;
    private final Provider<PlatformServerAdapter> platformServerAdapterProvider;

    @Getter
    private FilePack defaultFiles;

    public void init() {
        if (defaultFiles != null) return;

        Command command = loadFromResource(resolveResourcePath(FilePath.COMMAND), Command.class);
        Config config = loadFromResource(resolveResourcePath(FilePath.CONFIG), Config.class);
        Integration integration = loadFromResource(resolveResourcePath(FilePath.INTEGRATION), Integration.class);
        Message message = loadFromResource(resolveResourcePath(FilePath.MESSAGE), Message.class);
        Permission permission = loadFromResource(resolveResourcePath(FilePath.PERMISSION), Permission.class);

        Localization defaultEnglishLocalization = loadFromResource(resolveResourcePath(FilePath.LOCALIZATION_FOLDER.getPath() + DefaultLocalization.ENGLISH.getName() + ".yml"), Localization.class).withLanguage(DefaultLocalization.ENGLISH.getName());
        Localization defaultRussianLocalization = loadFromResource(resolveResourcePath(FilePath.LOCALIZATION_FOLDER.getPath() + DefaultLocalization.RUSSIAN.getName() + ".yml"), Localization.class).withLanguage(DefaultLocalization.RUSSIAN.getName());

        Map<String, Localization> localizations = new Object2ObjectArrayMap<>();
        localizations.put(DefaultLocalization.ENGLISH.getName(), defaultEnglishLocalization);
        localizations.put(DefaultLocalization.RUSSIAN.getName(), defaultRussianLocalization);
        for (Localization localization : loadLocalizationFiles(config, localizations, false).values()) {
            if (localizations.containsKey(localization.language())) continue;

            localizations.put(localization.language(), getDefaultLocalizationByLanguage(localization.language(), localizations));
        }

        defaultFiles = new FilePack(
                command,
                config,
                integration,
                message,
                permission,
                Map.copyOf(localizations)
        );

        fileWriter.save(defaultFiles, true);
    }

    public FilePack loadFiles(FilePack currentFiles) {
        currentFiles = currentFiles == null ? defaultFiles : currentFiles;

        Command command = loadOrDefault(FilePath.COMMAND.getPath(), currentFiles.command(), (command1, command2) ->
                commandMerger.merge(command1.toBuilder(), command2)
        );

        Config config = loadAndMergeConfig(currentFiles);

        Integration integration = loadOrDefault(FilePath.INTEGRATION.getPath(), currentFiles.integration(), (integration1, integration2) ->
                integrationMerger.merge(integration1.toBuilder(), integration2)
        );

        Message message = loadOrDefault(FilePath.MESSAGE.getPath(), currentFiles.message(), (message1, message2) ->
                messageMerger.merge(message1.toBuilder(), message2)
        );

        Permission permission = loadOrDefault(FilePath.PERMISSION.getPath(), currentFiles.permission(), (permission1, permission2) ->
                permissionMerger.merge(permission1.toBuilder(), permission2)
        );

        Map<String, Localization> localizations = loadLocalizationFiles(config, currentFiles.localizations(), true);

        return new FilePack(
                command,
                config,
                integration,
                message,
                permission,
                localizations
        );
    }

    public Config loadAndMergeConfig(FilePack currentFiles) {
        currentFiles = currentFiles == null ? defaultFiles : currentFiles;
        return loadOrDefault(FilePath.CONFIG.getPath(), currentFiles.config(), (config1, config2) ->
                configMerger.merge(config1.toBuilder(), config2)
        );
    }

    public Map<String, Localization> loadLocalizationFiles(Config config, Map<String, Localization> localizations, boolean merge) {
        Set<String> languages = new ObjectOpenHashSet<>(Arrays.stream(DefaultLocalization.values()).map(DefaultLocalization::getName).toList());
        languages.add(config.language().type());

        try (Stream<Path> paths = Files.walk(projectPath.resolve(Localization.FOLDER_NAME))) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                File localization = path.toFile();
                String localizationName = localization.getName();
                if (localizationName.endsWith(".yml")) {
                    languages.add(Strings.CS.replace(localizationName, ".yml", ""));
                }
            });
        } catch (NoSuchFileException ignored) {
            // ignore first startup
        } catch (IOException e) {
            fLogger.warning(e);
        }

        return languages.stream().collect(
                Collectors.toUnmodifiableMap(
                        language -> language,
                        language -> loadOrDefault(
                                FilePath.LOCALIZATION_FOLDER.getPath() + language + ".yml",
                                getDefaultLocalizationByLanguage(language, localizations),
                                (localization1, localization2) -> localizationMerger.merge(localization1.toBuilder(), localization2),
                                merge
                        ).withLanguage(language)
                )
        );
    }

    public <T> T loadOrDefault(String path, T defaultFile, BinaryOperator<T> mergeOperator) {
        return loadOrDefault(path, defaultFile, mergeOperator, true);
    }

    public <T> T loadOrDefault(String path, T defaultFile, BinaryOperator<T> mergeOperator, boolean merge) {
        Path pathToFile = Paths.get(projectPath.toString(), path);
        if (!Files.exists(pathToFile)) return defaultFile;
        if (pathToFile.toFile().lastModified() == FileWriter.LAST_MODIFIED_TIME) return defaultFile;

        Optional<T> file = load(pathToFile, defaultFile);
        if (!merge && file.isPresent()) return file.get();

        return file
                .map(localFile -> mergeOperator.apply(defaultFile, localFile))
                .orElse(defaultFile);
    }

    public <T> T loadFromResource(String path, Class<T> type) {
        try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream( "config/" + path)) {
            if (resourceAsStream == null) {
                throw new FileNotFoundException("Resource not found: " + path);
            }

            return yamlMapper.readValue(resourceAsStream, type);
        } catch (IOException e) {
            throw new FileLoadException(path, e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> load(Path pathToFile, T defaultFile) {
        File file = pathToFile.toFile();

        try {
            return Optional.of((T) yamlMapper.readValue(file, defaultFile.getClass()));
        } catch (Exception e) {
            if (e instanceof MismatchedInputException mismatchedInputException
                    && mismatchedInputException.getMessage() != null
                    && mismatchedInputException.getMessage().contains("No content to map due to end-of-input")) {
                fileWriter.save(pathToFile, defaultFile);
            } else {
                throw new FileLoadException(file.getPath(), e);
            }
        }

        return Optional.empty();
    }

    private String resolveResourcePath(FilePath filePath) {
        return resolveResourcePath(filePath.getPath());
    }

    private String resolveResourcePath(String path) {
        if (platformServerAdapterProvider.get().getPlatformType() == PlatformType.HYTALE) {
            return "hytale/" + path;
        }

        return "minecraft/" + path;
    }

    private Localization getDefaultLocalizationByLanguage(String language, Map<String, Localization> localizations) {
        return localizations.getOrDefault(language, language.toLowerCase().contains("ru")
                ? localizations.get(DefaultLocalization.RUSSIAN.getName())
                : localizations.get(DefaultLocalization.ENGLISH.getName())
        );
    }

}
