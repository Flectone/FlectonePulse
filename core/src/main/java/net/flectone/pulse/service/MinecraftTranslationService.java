package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
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

@Singleton
public class MinecraftTranslationService {

    private final String MINECRAFT_TRANSLATION_API = "https://assets.mcasset.cloud/<version>/assets/minecraft/lang/<language>";
    private final Map<String, String> translations = new HashMap<>();

    // FlectonePulse/localizations/minecraft/...
    private final Path translationPath;
    private final PacketProvider packetProvider;
    private final FileResolver fileResolver;
    private final FLogger fLogger;

    private final boolean isModern;

    private Translator translator;

    @Inject
    public MinecraftTranslationService(@Named("projectPath") Path projectPath,
                                       PacketProvider packetProvider,
                                       FileResolver fileResolver,
                                       FLogger fLogger) {
        this.translationPath = projectPath.resolve("localizations/minecraft");
        this.packetProvider = packetProvider;
        this.fileResolver = fileResolver;
        this.fLogger = fLogger;
        this.isModern = detectModernVersion();
    }

    public void reload() {
        translations.clear();
        downloadLocalizationFile();
        loadLocalization();
        initializeTranslations();
    }

    public String getVersion() {
        return packetProvider.getServerVersion().getReleaseName();
    }

    public String getLanguage() {
        return fileResolver.getConfig().getLanguage().getType().toLowerCase(Locale.ROOT);
    }

    @Nullable
    public String translate(String key) {
        return translations.get(key);
    }

    public void downloadLocalizationFile() {
        String language = getLanguage();
        String version = getVersion();

        String extension = isModern ? ".json" : ".lang";
        Path outputPath = translationPath.resolve(language + extension);

        if (Files.exists(outputPath)) return;

        String formattedLanguage = isModern ? language : formatLegacyLanguage(language);
        if (formattedLanguage == null) return;

        String url = buildLocalizationUrl(version, formattedLanguage, extension);
        downloadFile(url, outputPath);
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

    public void loadLocalization() {
        String language = getLanguage();
        Path localizationFile = translationPath.resolve(language + (isModern ? ".json" : ".lang"));

        if (!Files.exists(localizationFile)) return;

        try {
            Map<String, String> loadedTranslations = isModern
                    ? loadJsonTranslations(localizationFile)
                    : loadLegacyTranslations(localizationFile);

            translations.putAll(loadedTranslations);
            fLogger.info("Loaded translation /localization/minecraft/" + localizationFile.getFileName());
        } catch (IOException e) {
            fLogger.warning("Failed to load translations");
        }
    }

    private Map<String, String> loadJsonTranslations(Path file) throws IOException {
        return new ObjectMapper().readValue(file, new TypeReference<>() {});
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

    public void initializeTranslations() {
        if (translator != null) {
            GlobalTranslator.translator().removeSource(translator);
        }

        translator = createFlectonePulseTranslator();

        GlobalTranslator.translator().addSource(translator);
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
            fLogger.warning("Failed to detect Minecraft version translation");
        }

        return false;
    }

    private String formatLegacyLanguage(String language) {
        String[] parts = language.split("_");
        if (parts.length != 2) return null;
        return parts[0] + "_" + parts[1].toUpperCase(Locale.ROOT);
    }
}
