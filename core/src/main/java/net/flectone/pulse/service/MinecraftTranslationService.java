package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftTranslationService {

    private static final String MINECRAFT_TRANSLATION_API = "https://assets.mcasset.cloud/<version>/assets/minecraft/lang/<language>";

    private final Map<String, String> translations = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final @Named("minecraftPath") Path minecraftPath;
    private final PacketProvider packetProvider;
    private final FileFacade fileFacade;
    private final FLogger fLogger;

    private String language;
    private boolean isModern;
    private Translator translator;

    @Async(independent = true)
    public void reload() {
        String newLanguage = fileFacade.config().language().type().toLowerCase(Locale.ROOT);
        if (newLanguage.equals(language) && !translations.isEmpty()) return;

        language = newLanguage;
        isModern = detectModernVersion();
        translations.clear();

        if (downloadLocalizationFile()) {
            loadTranslations();
            initGlobalTranslator();
        }
    }

    public String getVersion() {
        return packetProvider.getServerVersion().getReleaseName();
    }

    @Nullable
    public String translate(String key) {
        return translations.get(key);
    }

    public boolean downloadLocalizationFile() {
        Path outputPath = resolveLocalizationFile();
        if (Files.exists(outputPath)) return true;

        String formattedLanguage = isModern ? language : formatLegacyLanguage(language);
        if (formattedLanguage == null) return false;

        String url = buildLocalizationUrl(getVersion(), formattedLanguage, isModern ? ".json" : ".lang");
        return downloadFile(url, outputPath);
    }

    public void loadTranslations() {
        Path localizationFile = resolveLocalizationFile();
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

    public void initGlobalTranslator() {
        if (translator != null) {
            GlobalTranslator.translator().removeSource(translator);
        }

        translator = createFlectonePulseTranslator();

        GlobalTranslator.translator().addSource(translator);
    }

    private boolean downloadFile(String fileUrl, Path outputPath) {
        try {
            HttpURLConnection connection = createConnection(fileUrl);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                fLogger.info("Downloading translation " + fileUrl);

                try (InputStream inputStream = connection.getInputStream()) {
                    Files.createDirectories(outputPath.getParent());
                    Files.copy(inputStream, outputPath);
                    return true;
                }
            }

            fLogger.warning("Failed to download translation. HTTP response: " + connection.getResponseCode() + " - " + fileUrl);
        } catch (IOException e) {
            fLogger.warning("Failed to download translation file");
        }

        return false;
    }

    private Path resolveLocalizationFile() {
        String extension = isModern ? ".json" : ".lang";
        return minecraftPath
                .resolve(getVersion())
                .resolve("lang")
                .resolve(language + extension);
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

    private Translator createFlectonePulseTranslator() {
        return new Translator() {

            @Override
            public @NotNull Key name() {
                return Key.key("flectonepulse:translation");
            }

            @Override
            public @Nullable MessageFormat translate(final @NotNull String key, final @NotNull Locale locale) {
                String translated = translations.get(key);
                if (translated == null) return null;

                return new MessageFormat(translated, locale);
            }

            @Override
            public @Nullable Component translate(@NotNull TranslatableComponent component, @NotNull Locale locale) {
                String translated = translations.get(component.key());
                if (translated == null) return null;

                return Component.text(translated).style(component.style());
            }

        };
    }

    private String buildLocalizationUrl(String version, String language, String extension) {
        return StringUtils.replaceEach(
                MINECRAFT_TRANSLATION_API + extension,
                new String[]{"<version>", "<language>"},
                new String[]{version, language}
        );
    }

    private HttpURLConnection createConnection(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        return connection;
    }

    private boolean detectModernVersion() {
        try {
            String url = buildLocalizationUrl(getVersion(), "en_us", ".json");
            HttpURLConnection connection = createConnection(url);
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
}
