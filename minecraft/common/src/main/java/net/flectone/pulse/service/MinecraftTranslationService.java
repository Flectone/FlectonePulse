package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.util.WebUtil;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftTranslationService implements TranslationService {

    private static final String MINECRAFT_TRANSLATION_API = "https://assets.mcasset.cloud/<version>/assets/minecraft/lang/<language>";

    private final Map<String, String> translations = new ConcurrentHashMap<>();
    private final @Named("defaultMapper") ObjectMapper objectMapper;
    private final @Named("minecraftPath") Path minecraftPath;
    private final PacketProvider packetProvider;
    private final FileFacade fileFacade;
    private final WebUtil webUtil;
    private final FLogger fLogger;
    private final TaskScheduler taskScheduler;

    private String lastLanguage;
    private Translator translator;

    @Override
    public void reload() {
        taskScheduler.runAsync(() -> {
            String newLanguage = getLastLanguage();
            if (newLanguage.equals(lastLanguage) && !translations.isEmpty()) return;

            lastLanguage = newLanguage;
            translations.clear();

            boolean isModern = detectModernVersion();
            if (downloadLocalizationFile(isModern, lastLanguage)) {
                loadTranslations(isModern);
                initGlobalTranslator();
            }

        }, true);
    }

    @Override
    public void initGlobalTranslator() {
        if (translator != null) {
            GlobalTranslator.translator().removeSource(translator);
        }

        translator = new FlectoneTranslator(translations);

        GlobalTranslator.translator().addSource(translator);
    }

    public boolean downloadLocalizationFile(boolean isModern, String language) {
        Path outputPath = resolveLocalizationFile(isModern);
        if (Files.exists(outputPath)) return true;

        String formattedLanguage = isModern ? language : formatLegacyLanguage(language);
        if (formattedLanguage == null) return false;

        String url = buildLocalizationUrl(getVersion(), formattedLanguage, isModern ? ".json" : ".lang");
        return webUtil.downloadFile(url, outputPath);
    }

    public void loadTranslations(boolean isModern) {
        Path localizationFile = resolveLocalizationFile(isModern);
        if (!Files.exists(localizationFile)) return;

        try {
            Map<String, String> loadedTranslations = isModern
                    ? loadJsonTranslations(localizationFile)
                    : loadLegacyTranslations(localizationFile);

            translations.putAll(loadedTranslations);
            fLogger.info("Loaded translation " + localizationFile.getFileName());
        } catch (Exception e) {
            fLogger.warning("Failed to load translations");
        }
    }

    private String getLastLanguage() {
        return fileFacade.config().language().type().toLowerCase(Locale.ROOT);
    }

    private String getVersion() {
        return packetProvider.getServerVersion().getReleaseName();
    }

    private Path resolveLocalizationFile(boolean isModern) {
        String extension = isModern ? ".json" : ".lang";
        return minecraftPath
                .resolve(getVersion())
                .resolve("lang")
                .resolve(lastLanguage + extension);
    }

    private Map<String, String> loadJsonTranslations(Path file) {
        return objectMapper.readValue(file, new TypeReference<>() {});
    }

    private Map<String, String> loadLegacyTranslations(Path file) throws IOException {
        Map<String, String> result = new HashMap<>();
        Files.readAllLines(file).forEach(line -> {
            String[] parts = line.split("=", 2);
            if (parts.length == 2) {
                result.put(parts[0], parts[1]);
            }
        });

        return result;
    }

    private String buildLocalizationUrl(String version, String language, String extension) {
        return StringUtils.replaceEach(
                MINECRAFT_TRANSLATION_API + extension,
                new String[]{"<version>", "<language>"},
                new String[]{version, language}
        );
    }

    private boolean detectModernVersion() {
        try {
            String url = buildLocalizationUrl(getVersion(), "en_us", ".json");
            HttpURLConnection connection = webUtil.createConnection(url);
            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            // legacy version
            return false;
        }
    }

    private String formatLegacyLanguage(String language) {
        String[] parts = language.split("_");
        if (parts.length != 2) return null;
        return parts[0] + "_" + parts[1].toUpperCase(Locale.ROOT);
    }

    private record FlectoneTranslator(Map<String, String> translations) implements Translator {

        @Override
        public @NonNull Key name() {
            return Key.key("flectonepulse:translation");
        }

        @Override
        public @Nullable MessageFormat translate(final @NonNull String key, final @NonNull Locale locale) {
            String translated = translations.get(key);
            if (translated == null) return null;

            return new MessageFormat(translated, locale);
        }

        @Override
        public @Nullable Component translate(@NonNull TranslatableComponent component, @NonNull Locale locale) {
            String translated = translations.get(component.key());
            if (translated == null) return null;

            return Component.text(translated).mergeStyle(component);
        }

    }
}
