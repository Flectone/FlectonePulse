package net.flectone.pulse.service;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.platform.registry.CacheRegistry;
import net.flectone.pulse.util.constant.CacheName;
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
    private volatile ExecutorService executorService = Executors.newFixedThreadPool(4);

    private Cache<String, String> getCache() {
        return cacheRegistry.getCache(CacheName.TRANSLATION_CACHE);
    }

    /**
     * Generate cache key from source language, target language and text.
     */
    private String getCacheKey(String sourceLang, String targetLang, String text) {
        return sourceLang + ":" + targetLang + ":" + text;
    }

    /**
     * Get translation from cache.
     */
    public @Nullable String get(String sourceLang, String targetLang, String text) {
        String key = getCacheKey(sourceLang, targetLang, text);
        return getCache().getIfPresent(key);
    }

    /**
     * Put translation to cache.
     */
    public void put(String sourceLang, String targetLang, String text, String translation) {
        String key = getCacheKey(sourceLang, targetLang, text);
        getCache().put(key, translation);
    }

    /**
     * Translate text using MyMemory API.
     * Returns null if translation failed.
     */
    public @Nullable String translateWithMyMemory(String sourceLang, String targetLang, String text) {
        try {
            // Normalize language codes (en_us -> en, ru_ru -> ru)
            String normalizedSource = normalizeLangCode(sourceLang);
            String normalizedTarget = normalizeLangCode(targetLang);

            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String urlString = "https://api.mymemory.translated.net/get?q=" + encodedText
                + "&langpair=" + normalizedSource + "|" + normalizedTarget;

            URI uri = new URI(urlString);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "FlectonePulse/1.9.4");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse JSON response: {"responseData":{"translatedText":"..."}}
            String jsonResponse = response.toString();
            int startIndex = jsonResponse.indexOf("\"translatedText\":\"");
            if (startIndex == -1) {
                return null;
            }

            startIndex += 18; // length of "translatedText":"
            int endIndex = jsonResponse.indexOf("\"", startIndex);
            if (endIndex == -1) {
                return null;
            }

            String translation = jsonResponse.substring(startIndex, endIndex);

            // Unescape JSON string
            translation = translation.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");

            return translation;
        } catch (Exception _) {
            return null;
        }
    }

    /**
     * Translate text asynchronously using MyMemory API.
     */
    public CompletableFuture<String> translateWithMyMemoryAsync(String sourceLang, String targetLang, String text) {
        return CompletableFuture.supplyAsync(() -> {
            String translation = translateWithMyMemory(sourceLang, targetLang, text);
            if (translation != null && !translation.isEmpty()) {
                put(sourceLang, targetLang, text, translation);
            }
            return translation;
        }, executorService);
    }

    /**
     * Normalize language code from minecraft format to ISO 639-1.
     * Examples: en_us -> en, ru_ru -> ru, zh_cn -> zh
     */
    private String normalizeLangCode(String langCode) {
        if (langCode == null || langCode.isEmpty()) {
            return "en";
        }

        // Extract first part before underscore
        int underscoreIndex = langCode.indexOf('_');
        if (underscoreIndex > 0) {
            return langCode.substring(0, underscoreIndex).toLowerCase();
        }

        return langCode.toLowerCase();
    }

    /**
     * Shutdown executor service.
     */
    public void shutdown() {
        ExecutorService old = executorService;
        executorService = Executors.newFixedThreadPool(4);
        old.shutdown();
    }
}
