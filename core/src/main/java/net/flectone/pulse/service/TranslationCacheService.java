package net.flectone.pulse.service;

import com.google.common.cache.Cache;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.registry.CacheRegistry;
import net.flectone.pulse.util.constant.CacheName;
import net.flectone.pulse.util.logging.FLogger;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TranslationCacheService {

    /** Base endpoint of the MyMemory public translation API. */
    private static final String MYMEMORY_API_URL = "https://api.mymemory.translated.net/get";
    /** Base endpoint of the free Google Translate gtx API. */
    private static final String GOOGLE_TRANSLATE_API_URL = "https://translate.googleapis.com/translate_a/single";

    /**
     * Minecraft locales whose region is meaningful and accepted by both Google gtx
     * and MyMemory (hyphenated, uppercase region). For these we keep the region
     * instead of collapsing to the base ISO 639-1 code.
     */
    private static final Map<String, String> REGION_AWARE_LOCALES = Map.of(
            "zh_cn", "zh-CN",
            "zh_tw", "zh-TW",
            "zh_hk", "zh-TW",
            "pt_br", "pt-BR",
            "pt_pt", "pt-PT"
    );

    private final CacheRegistry cacheRegistry;
    private final FLogger fLogger;
    private final Gson gson;
    private final Provider<IntegrationModule> integrationModuleProvider;
    private volatile ExecutorService executorService = Executors.newFixedThreadPool(4);

    /** Minimal MyMemory response shape — only the fields actually read. */
    private record MyMemoryResponse(MyMemoryResponseData responseData) {}

    private record MyMemoryResponseData(String translatedText) {}

    /** In-flight requests deduplication — same (source, target, text) returns the same future. */
    private final Map<String, CompletableFuture<String>> inFlight = new ConcurrentHashMap<>();

    private Cache<String, String> getCache() {
        return cacheRegistry.getCache(CacheName.TRANSLATION_CACHE);
    }

    private String getCacheKey(String sourceLang, String targetLang, String text) {
        return sourceLang + ":" + targetLang + ":" + text;
    }

    public @Nullable String get(String sourceLang, String targetLang, String text) {
        String key = getCacheKey(sourceLang, targetLang, text);
        String cached = getCache().getIfPresent(key);
        return cached;
    }

    public void put(String sourceLang, String targetLang, String text, String translation) {
        String key = getCacheKey(sourceLang, targetLang, text);
        getCache().put(key, translation);
    }

    /**
     * Translate via MyMemory public API. Returns null on any failure.
     * Response JSON is parsed with Gson so unicode escapes are decoded properly.
     */
    public @Nullable String translateWithMyMemory(String sourceLang, String targetLang, String text) {
        try {
            String normalizedSource = normalizeLangCode(sourceLang);
            String normalizedTarget = normalizeLangCode(targetLang);

            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            // Pipe is encoded as %7C — java.net.URI (RFC 3986 strict) rejects raw pipe in query.
            String urlString = MYMEMORY_API_URL + "?q=" + encodedText
                + "&langpair=" + normalizedSource + "%7C" + normalizedTarget;

            URI uri = new URI(urlString);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "FlectonePulse/1.9.4");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            if (responseCode != 200) {
                fLogger.warning("[AutoTranslate] MyMemory: non-200 response (%d) for %s→%s, returning null",
                        responseCode, normalizedSource, normalizedTarget);
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String jsonResponse = response.toString();
            MyMemoryResponse parsed = gson.fromJson(jsonResponse, MyMemoryResponse.class);
            if (parsed == null || parsed.responseData() == null) {
                fLogger.warning("[AutoTranslate] MyMemory: missing responseData in JSON");
                return null;
            }
            String translation = parsed.responseData().translatedText();
            if (translation == null) {
                fLogger.warning("[AutoTranslate] MyMemory: missing translatedText in responseData");
                return null;
            }

            return translation;
        } catch (Exception e) {
            fLogger.warning(e, "[AutoTranslate] MyMemory: exception during request for %s",
                    sourceLang + "→" + targetLang);
            return null;
        }
    }

    public CompletableFuture<String> translateWithMyMemoryAsync(String sourceLang, String targetLang, String text) {
        return submitAsync("MyMemory", sourceLang, targetLang, text, this::translateWithMyMemory);
    }

    public CompletableFuture<String> translateWithGoogleAsync(String sourceLang, String targetLang, String text) {
        return submitAsync("Google", sourceLang, targetLang, text, this::translateWithGoogle);
    }

    /**
     * Run the configured provider chain — try each in order, return the first
     * non-null/non-empty translation that differs from the input text. Any 200-OK
     * with text==input or any error response moves on to the next provider.
     *
     * <p>De-duplicated by (source, target, text) — concurrent requests for the
     * same key share a single future. Empty providers list → warning with a
     * config example, auto-translate is effectively disabled for this call.
     */
    public CompletableFuture<String> translateAsync(String sourceLang,
                                                    String targetLang,
                                                    String text,
                                                    @Nullable List<String> providers) {
        String cached = get(sourceLang, targetLang, text);
        if (cached != null && !cached.isEmpty()) {
            return CompletableFuture.completedFuture(cached);
        }

        String key = sourceLang + ":" + targetLang + ":" + text;
        CompletableFuture<String> existing = inFlight.get(key);
        if (existing != null) {
            return existing;
        }

        if (providers == null || providers.isEmpty()) {
            fLogger.warning("[AutoTranslate] chain: no providers configured. Add to config/message.yml under format.translate:%n"
                    + "  providers:%n"
                    + "    - GOOGLE%n"
                    + "    - MYMEMORY%n"
                    + "    - DEEPL%n"
                    + "    - YANDEX%n"
                    + "Auto-translate disabled until configured.");
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<String> future = CompletableFuture.supplyAsync(
                () -> iterateProviders(sourceLang, targetLang, text, providers),
                executorService
        );
        inFlight.put(key, future);
        future.whenComplete((result, throwable) -> inFlight.remove(key));
        return future;
    }

    private @Nullable String iterateProviders(String sourceLang, String targetLang, String text, List<String> providers) {
        for (String provider : providers) {
            String upper = provider == null ? "" : provider.trim().toUpperCase();

            String result = callProvider(upper, sourceLang, targetLang, text);
            if (result != null && !result.isEmpty() && !result.equals(text)) {
                put(sourceLang, targetLang, text, result);
                return result;
            }
        }
        fLogger.warning("[AutoTranslate] chain %s→%s: ALL providers failed for text='%s'",
                sourceLang, targetLang, text);
        return null;
    }

    private @Nullable String callProvider(String provider, String sourceLang, String targetLang, String text) {
        try {
            return switch (provider) {
                case "GOOGLE" -> translateWithGoogle(sourceLang, targetLang, text);
                case "MYMEMORY" -> translateWithMyMemory(sourceLang, targetLang, text);
                case "DEEPL" -> integrationModuleProvider.get().deeplTranslate(FPlayer.UNKNOWN, sourceLang, targetLang, text);
                case "YANDEX" -> integrationModuleProvider.get().yandexTranslate(FPlayer.UNKNOWN, sourceLang, targetLang, text);
                default -> {
                    fLogger.warning("[AutoTranslate] chain: unknown provider name '%s' (allowed: GOOGLE, MYMEMORY, DEEPL, YANDEX)", provider);
                    yield null;
                }
            };
        } catch (Exception e) {
            fLogger.warning(e, "[AutoTranslate] chain: provider %s threw exception", provider);
            return null;
        }
    }

    private CompletableFuture<String> submitAsync(String providerLabel,
                                                  String sourceLang,
                                                  String targetLang,
                                                  String text,
                                                  TranslateFn fn) {
        String cached = get(sourceLang, targetLang, text);
        if (cached != null && !cached.isEmpty()) {
            return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> {
            String translation = fn.translate(sourceLang, targetLang, text);
            if (translation != null && !translation.isEmpty()) {
                put(sourceLang, targetLang, text, translation);
            }
            return translation;
        }, executorService);
    }

    @FunctionalInterface
    private interface TranslateFn {
        @Nullable String translate(String sourceLang, String targetLang, String text);
    }

    /**
     * Translate via free Google Translate gtx endpoint — no API key required.
     * Used as the default auto-translate provider; MyMemory is opt-in because
     * it tends to return Azerbaijani/etc on auto-detect and has a 1000 word/day
     * IP rate limit.
     */
    public @Nullable String translateWithGoogle(String sourceLang, String targetLang, String text) {
        try {
            String normalizedSource = normalizeLangCode(sourceLang);
            String normalizedTarget = normalizeLangCode(targetLang);

            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String urlString = GOOGLE_TRANSLATE_API_URL + "?client=gtx"
                    + "&sl=" + normalizedSource
                    + "&tl=" + normalizedTarget
                    + "&dt=t&ie=UTF-8&oe=UTF-8&q=" + encodedText;

            URI uri = new URI(urlString);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; FlectonePulse/1.9.4)");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            if (responseCode != 200) {
                fLogger.warning("[AutoTranslate] Google: non-200 response (%d), returning null", responseCode);
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String jsonResponse = response.toString();
            // Response shape: [[["translated text","original",null,null,1]],null,"en",...]
            JsonArray root = gson.fromJson(jsonResponse, JsonArray.class);
            if (root == null) {
                fLogger.warning("[AutoTranslate] Google: response is not a JSON array");
                return null;
            }
            JsonElement segments = root.get(0);
            if (segments == null || !segments.isJsonArray()) {
                fLogger.warning("[AutoTranslate] Google: missing segments array");
                return null;
            }

            StringBuilder translatedBuilder = new StringBuilder();
            segments.getAsJsonArray().forEach(seg -> {
                if (seg.isJsonArray() && seg.getAsJsonArray().size() > 0) {
                    JsonElement piece = seg.getAsJsonArray().get(0);
                    if (piece != null && !piece.isJsonNull()) {
                        translatedBuilder.append(piece.getAsString());
                    }
                }
            });
            String translation = translatedBuilder.toString();

            return translation.isEmpty() ? null : translation;
        } catch (Exception e) {
            fLogger.warning(e, "[AutoTranslate] Google: exception during request for %s",
                    sourceLang + "→" + targetLang);
            return null;
        }
    }

    /**
     * Normalize language code from minecraft format to ISO 639-1.
     * Examples: en_us → en, ru_ru → ru.
     * Region-aware locales keep their region (zh_cn → zh-CN, pt_br → pt-BR) since
     * both Google gtx and MyMemory accept and need it to disambiguate.
     */
    private String normalizeLangCode(String langCode) {
        if (langCode == null || langCode.isEmpty()) {
            return "en";
        }
        String key = langCode.toLowerCase().replace('-', '_');
        String regionAware = REGION_AWARE_LOCALES.get(key);
        if (regionAware != null) {
            return regionAware;
        }
        int underscoreIndex = key.indexOf('_');
        if (underscoreIndex > 0) {
            return key.substring(0, underscoreIndex);
        }
        return key;
    }

    public void shutdown() {
        ExecutorService old = executorService;
        executorService = Executors.newFixedThreadPool(4);
        old.shutdown();
    }
}
