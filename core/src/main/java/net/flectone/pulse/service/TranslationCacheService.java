package net.flectone.pulse.service;

import com.google.common.cache.Cache;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.registry.CacheRegistry;
import net.flectone.pulse.util.WebUtil;
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
import java.util.concurrent.TimeUnit;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TranslationCacheService {

    private static final String MYMEMORY_API_URL = "https://api.mymemory.translated.net/get";
    private static final String GOOGLE_TRANSLATE_API_URL = "https://translate.googleapis.com/translate_a/single";

    // Locales whose region is accepted by both providers — keep the region instead
    // of collapsing to the base ISO 639-1 code.
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

    // Same (source, target, text) shares one future while a request is running.
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
            try {
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", WebUtil.USER_AGENT);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();

                if (responseCode != 200) {
                    fLogger.warning("[AutoTranslate] MyMemory: non-200 response (%d) for %s→%s, returning null",
                            responseCode, normalizedSource, normalizedTarget);
                    return null;
                }

                String jsonResponse;
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    jsonResponse = response.toString();
                }

                return parseMyMemoryResponse(jsonResponse);
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            fLogger.warning(e, "[AutoTranslate] MyMemory: exception during request for %s",
                    sourceLang + "→" + targetLang);
            return null;
        }
    }

    // MyMemory answers with HTTP 200 even on errors (quota, invalid language),
    // signalling failure only inside the body: responseStatus != 200 and/or a
    // "MYMEMORY WARNING" placeholder in translatedText. Returning null on those
    // lets iterateProviders move to the next provider instead of caching garbage
    // for an hour. responseStatus arrives as a number (200) or string ("200")
    // depending on the endpoint, so both forms are handled.
    @Nullable String parseMyMemoryResponse(String jsonResponse) {
        JsonObject root;
        try {
            root = gson.fromJson(jsonResponse, JsonObject.class);
        } catch (Exception e) {
            fLogger.warning("[AutoTranslate] MyMemory: response is not valid JSON");
            return null;
        }
        if (root == null) {
            fLogger.warning("[AutoTranslate] MyMemory: empty JSON response");
            return null;
        }

        JsonElement statusElement = root.get("responseStatus");
        if (statusElement != null && statusElement.isJsonPrimitive()) {
            String status = statusElement.getAsString().trim();
            if (!"200".equals(status)) {
                fLogger.warning("[AutoTranslate] MyMemory: responseStatus=%s (not 200), returning null", status);
                return null;
            }
        }

        JsonElement dataElement = root.get("responseData");
        if (dataElement == null || !dataElement.isJsonObject()) {
            fLogger.warning("[AutoTranslate] MyMemory: missing responseData in JSON");
            return null;
        }
        JsonElement textElement = dataElement.getAsJsonObject().get("translatedText");
        if (textElement == null || textElement.isJsonNull() || !textElement.isJsonPrimitive()) {
            fLogger.warning("[AutoTranslate] MyMemory: missing translatedText in responseData");
            return null;
        }

        String translation = textElement.getAsString();
        if (translation.startsWith("MYMEMORY WARNING")) {
            fLogger.warning("[AutoTranslate] MyMemory: warning placeholder instead of translation: %s", translation);
            return null;
        }

        return translation;
    }

    public CompletableFuture<String> translateWithMyMemoryAsync(String sourceLang, String targetLang, String text) {
        return submitAsync("MyMemory", sourceLang, targetLang, text, this::translateWithMyMemory);
    }

    // Tries each provider in order, returns the first translation that differs from
    // the input. Concurrent requests for the same key share one future (inFlight).
    public CompletableFuture<String> translateAsync(String sourceLang,
                                                    String targetLang,
                                                    String text,
                                                    @Nullable List<String> providers) {
        String cached = get(sourceLang, targetLang, text);
        if (cached != null && !cached.isEmpty()) {
            return CompletableFuture.completedFuture(cached);
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

        String key = sourceLang + ":" + targetLang + ":" + text;
        // computeIfAbsent makes check-then-act atomic: only one thread per key
        // creates the future and starts a network request; the rest share it.
        return inFlight.computeIfAbsent(key, k -> {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(
                    () -> iterateProviders(sourceLang, targetLang, text, providers),
                    executorService
            );
            // remove(k, future) drops only THIS future, never a newer one that a
            // later request may have installed for the same key.
            future.whenComplete((result, throwable) -> inFlight.remove(k, future));
            return future;
        });
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

    // Free Google Translate gtx endpoint — no API key required, default provider.
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
            try {
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", WebUtil.USER_AGENT);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();

                if (responseCode != 200) {
                    fLogger.warning("[AutoTranslate] Google: non-200 response (%d), returning null", responseCode);
                    return null;
                }

                String jsonResponse;
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    jsonResponse = response.toString();
                }

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
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            fLogger.warning(e, "[AutoTranslate] Google: exception during request for %s",
                    sourceLang + "→" + targetLang);
            return null;
        }
    }

    // minecraft locale → ISO 639-1 (en_us → en), except region-aware locales which
    // keep their region (zh_cn → zh-CN) since providers need it to disambiguate.
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
        // Swap in a fresh pool first so the service stays usable after a reload,
        // then hard-stop the old one and drop any in-flight futures — lingering
        // 5s HTTP tasks must not keep writing to the cache/history after disable.
        ExecutorService old = executorService;
        executorService = Executors.newFixedThreadPool(4);
        inFlight.clear();
        old.shutdownNow();
        try {
            old.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
