package net.flectone.pulse.service;

import com.google.common.cache.Cache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TranslationCacheService {

    private final CacheRegistry cacheRegistry;
    private final FLogger fLogger;
    private volatile ExecutorService executorService = Executors.newFixedThreadPool(4);

    private Cache<String, String> getCache() {
        return cacheRegistry.getCache(CacheName.TRANSLATION_CACHE);
    }

    private String getCacheKey(String sourceLang, String targetLang, String text) {
        return sourceLang + ":" + targetLang + ":" + text;
    }

    public @Nullable String get(String sourceLang, String targetLang, String text) {
        String key = getCacheKey(sourceLang, targetLang, text);
        String cached = getCache().getIfPresent(key);
        fLogger.info("[AutoTranslate] cache GET %s → %s", key, cached == null ? "MISS" : "HIT='" + cached + "'");
        return cached;
    }

    public void put(String sourceLang, String targetLang, String text, String translation) {
        String key = getCacheKey(sourceLang, targetLang, text);
        getCache().put(key, translation);
        fLogger.info("[AutoTranslate] cache PUT %s → '%s'", key, translation);
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
            String urlString = "https://api.mymemory.translated.net/get?q=" + encodedText
                + "&langpair=" + normalizedSource + "%7C" + normalizedTarget;

            fLogger.info("[AutoTranslate] MyMemory: GET %s", urlString);

            URI uri = new URI(urlString);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "FlectonePulse/1.9.4");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            fLogger.info("[AutoTranslate] MyMemory: response code=%d for %s→%s", responseCode, normalizedSource, normalizedTarget);

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
            String preview = jsonResponse.length() > 300 ? jsonResponse.substring(0, 300) + "...[truncated]" : jsonResponse;
            fLogger.info("[AutoTranslate] MyMemory: response body (preview)=%s", preview);

            JsonElement root = new JsonParser().parse(jsonResponse);
            if (!root.isJsonObject()) {
                fLogger.warning("[AutoTranslate] MyMemory: response is not a JSON object");
                return null;
            }
            JsonObject responseData = root.getAsJsonObject().getAsJsonObject("responseData");
            if (responseData == null) {
                fLogger.warning("[AutoTranslate] MyMemory: missing responseData in JSON");
                return null;
            }
            JsonElement translatedTextEl = responseData.get("translatedText");
            if (translatedTextEl == null || translatedTextEl.isJsonNull()) {
                fLogger.warning("[AutoTranslate] MyMemory: missing translatedText in responseData");
                return null;
            }
            String translation = translatedTextEl.getAsString();

            fLogger.info("[AutoTranslate] MyMemory: parsed translation %s→%s = '%s'",
                    normalizedSource, normalizedTarget, translation);

            return translation;
        } catch (Exception e) {
            fLogger.warning(e, "[AutoTranslate] MyMemory: exception during request for %s",
                    sourceLang + "→" + targetLang);
            return null;
        }
    }

    public CompletableFuture<String> translateWithMyMemoryAsync(String sourceLang, String targetLang, String text) {
        String cached = get(sourceLang, targetLang, text);
        if (cached != null && !cached.isEmpty()) {
            fLogger.info("[AutoTranslate] async %s→%s: using cached translation, skip API call", sourceLang, targetLang);
            return CompletableFuture.completedFuture(cached);
        }

        fLogger.info("[AutoTranslate] async %s→%s: submitting MyMemory API task to executor", sourceLang, targetLang);
        return CompletableFuture.supplyAsync(() -> {
            fLogger.info("[AutoTranslate] async %s→%s: executor started task on thread=%s",
                    sourceLang, targetLang, Thread.currentThread().getName());
            String translation = translateWithMyMemory(sourceLang, targetLang, text);
            if (translation != null && !translation.isEmpty()) {
                put(sourceLang, targetLang, text, translation);
            } else {
                fLogger.info("[AutoTranslate] async %s→%s: API returned null/empty, not caching", sourceLang, targetLang);
            }
            return translation;
        }, executorService);
    }

    /**
     * Normalize language code from minecraft format to ISO 639-1.
     * Examples: en_us → en, ru_ru → ru, zh_cn → zh.
     */
    private String normalizeLangCode(String langCode) {
        if (langCode == null || langCode.isEmpty()) {
            return "en";
        }
        int underscoreIndex = langCode.indexOf('_');
        if (underscoreIndex > 0) {
            return langCode.substring(0, underscoreIndex).toLowerCase();
        }
        return langCode.toLowerCase();
    }

    public void shutdown() {
        ExecutorService old = executorService;
        executorService = Executors.newFixedThreadPool(4);
        old.shutdown();
    }
}
