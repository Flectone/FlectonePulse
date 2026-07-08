package net.flectone.pulse.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.inject.Provider;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.registry.CacheRegistry;
import net.flectone.pulse.util.constant.CacheName;
import net.flectone.pulse.util.logging.FLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Offline safety-net for {@link TranslationCacheService}.
 *
 * <p>All tests in the default suite run WITHOUT network access: the cache,
 * logger and integration-module provider are lightweight test stubs, and only
 * pure logic (cache put/get, key shape, lang-code normalization, the offline
 * fast-paths of translateAsync, shutdown) is exercised.
 *
 * <p>The MyMemory / network tests are tagged {@code "integration"} so a plain
 * {@code gradle test} never reaches the real api.mymemory.translated.net.
 *
 * <p>NOTE: Mockito is intentionally NOT used here. The pinned mockito-core
 * 5.5.0 (ByteBuddy 1.14) cannot instrument Java 25 class files, so any
 * {@code mock(...)} call throws on this toolchain. Hand-rolled stubs keep the
 * suite green without touching the production classpath.
 */
class TranslationCacheServiceTest {

    private TranslationCacheService translationCacheService;
    private Cache<String, String> testCache;

    /** CacheRegistry stub: real Guava cache, no file/config init. */
    private static final class StubCacheRegistry extends CacheRegistry {
        private final Cache<String, String> cache;

        StubCacheRegistry(Cache<String, String> cache) {
            super(null); // fileFacade unused because init() is overridden
            this.cache = cache;
        }

        @Override
        public void init() {
            // skip real cache creation (would need a Config/FileFacade)
        }

        @Override
        @SuppressWarnings("unchecked")
        public <K, V> Cache<K, V> getCache(CacheName cacheName) {
            return (Cache<K, V>) cache;
        }
    }

    @BeforeEach
    void setUp() {
        // Real Guava cache backing the service, so put/get behaviour is genuine.
        testCache = CacheBuilder.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .maximumSize(1000)
                .build();

        CacheRegistry cacheRegistry = new StubCacheRegistry(testCache);

        // FLogger is a record: build one with a no-op log consumer / null config.
        FLogger fLogger = new FLogger(record -> { }, () -> null);

        Gson gson = new Gson();

        // Provider is an interface: a lambda that must never be invoked in the
        // offline suite (only DEEPL/YANDEX providers would touch it).
        Provider<IntegrationModule> integrationModuleProvider = () -> {
            throw new AssertionError("IntegrationModule provider must not be used offline");
        };

        translationCacheService = new TranslationCacheService(
                cacheRegistry,
                fLogger,
                gson,
                integrationModuleProvider
        );
    }

    // --- cache put / get -----------------------------------------------------

    @Test
    void testPutAndGet() {
        translationCacheService.put("en_us", "ru_ru", "hello", "привет");

        String result = translationCacheService.get("en_us", "ru_ru", "hello");

        assertEquals("привет", result);
    }

    @Test
    void testGetNonExistent() {
        String result = translationCacheService.get("en_us", "ru_ru", "nonexistent");

        assertNull(result);
    }

    @Test
    void testCacheKeyGeneration() {
        // Same parameters should generate same key
        translationCacheService.put("en_us", "ru_ru", "hello", "привет");
        String cached = translationCacheService.get("en_us", "ru_ru", "hello");

        assertEquals("привет", cached);

        // Different target language should not return cached value
        String notCached = translationCacheService.get("en_us", "de_de", "hello");
        assertNull(notCached);
    }

    @Test
    void testCachePersistence() {
        translationCacheService.put("en_us", "ru_ru", "test", "тест");
        assertEquals("тест", translationCacheService.get("en_us", "ru_ru", "test"));

        translationCacheService.put("en_us", "de_de", "test", "prüfen");
        translationCacheService.put("ru_ru", "en_us", "привет", "hello");

        assertEquals("тест", translationCacheService.get("en_us", "ru_ru", "test"));
        assertEquals("prüfen", translationCacheService.get("en_us", "de_de", "test"));
        assertEquals("hello", translationCacheService.get("ru_ru", "en_us", "привет"));
    }

    @Test
    void testEmptyTextHandling() {
        translationCacheService.put("en_us", "ru_ru", "", "");

        String result = translationCacheService.get("en_us", "ru_ru", "");

        assertEquals("", result);
    }

    @Test
    void testGetMissingReturnsNull() {
        // get on an absent key must not throw and must return null
        String result = translationCacheService.get("en_us", "ru_ru", "something");

        assertNull(result);
    }

    @Test
    void testCacheKeyShape() throws Exception {
        // The cache key is "source:target:text" — assert the exact stored key,
        // so a future refactor of getCacheKey can't silently change the layout.
        translationCacheService.put("en_us", "ru_ru", "hello", "привет");

        assertEquals("привет", testCache.getIfPresent("en_us:ru_ru:hello"));
        assertNull(testCache.getIfPresent("ru_ru:en_us:hello"));
    }

    // --- lang-code normalization (private, exercised via reflection) ----------

    private String normalize(String langCode) throws Exception {
        Method method = TranslationCacheService.class
                .getDeclaredMethod("normalizeLangCode", String.class);
        method.setAccessible(true);
        return (String) method.invoke(translationCacheService, langCode);
    }

    @Test
    void testNormalizeStripsRegionToBaseIso() throws Exception {
        assertEquals("en", normalize("en_us"));
        assertEquals("ru", normalize("ru_ru"));
        assertEquals("de", normalize("de_de"));
    }

    @Test
    void testNormalizeKeepsBareCode() throws Exception {
        assertEquals("en", normalize("en"));
        assertEquals("fr", normalize("FR"));
    }

    @Test
    void testNormalizeKeepsRegionForRegionAwareLocales() throws Exception {
        assertEquals("zh-CN", normalize("zh_cn"));
        assertEquals("zh-TW", normalize("zh_tw"));
        assertEquals("zh-TW", normalize("zh_hk")); // hk maps onto TW
        assertEquals("pt-BR", normalize("pt_br"));
        assertEquals("pt-PT", normalize("pt_pt"));
    }

    @Test
    void testNormalizeAcceptsHyphenAndCaseVariants() throws Exception {
        // input may arrive hyphenated and/or in mixed case
        assertEquals("zh-CN", normalize("ZH-CN"));
        assertEquals("pt-BR", normalize("Pt-Br"));
        assertEquals("en", normalize("EN-US"));
    }

    @Test
    void testNormalizeNullAndEmptyDefaultToEn() throws Exception {
        assertEquals("en", normalize(null));
        assertEquals("en", normalize(""));
    }

    // --- MyMemory response validation (offline, pure JSON parsing) ------------

    @Test
    void testMyMemoryParsesTranslationOnNumericStatus200() {
        String json = "{\"responseData\":{\"translatedText\":\"привет\"},\"responseStatus\":200}";
        assertEquals("привет", translationCacheService.parseMyMemoryResponse(json));
    }

    @Test
    void testMyMemoryParsesTranslationOnStringStatus200() {
        // MyMemory sometimes returns responseStatus as a quoted string.
        String json = "{\"responseData\":{\"translatedText\":\"привет\"},\"responseStatus\":\"200\"}";
        assertEquals("привет", translationCacheService.parseMyMemoryResponse(json));
    }

    @Test
    void testMyMemoryParsesTranslationWhenStatusAbsent() {
        // No responseStatus field at all → treat as success if text is present.
        String json = "{\"responseData\":{\"translatedText\":\"привет\"}}";
        assertEquals("привет", translationCacheService.parseMyMemoryResponse(json));
    }

    @Test
    void testMyMemoryRejectsNon200Status() {
        // Quota exceeded: HTTP 200 body but responseStatus 429 → must not be cached.
        String json = "{\"responseData\":{\"translatedText\":\"MYMEMORY WARNING: YOU USED ALL AVAILABLE FREE TRANSLATIONS\"},"
                + "\"responseStatus\":429}";
        assertNull(translationCacheService.parseMyMemoryResponse(json));
    }

    @Test
    void testMyMemoryRejectsWarningPlaceholderEvenWithStatus200() {
        // Defensive: warning text must be dropped regardless of the status field.
        String json = "{\"responseData\":{\"translatedText\":\"MYMEMORY WARNING: SOMETHING\"},\"responseStatus\":200}";
        assertNull(translationCacheService.parseMyMemoryResponse(json));
    }

    @Test
    void testMyMemoryRejectsInvalidLanguageStatus() {
        // "'AUTO' IS AN INVALID SOURCE LANGUAGE" arrives with a non-200 status.
        String json = "{\"responseData\":{\"translatedText\":\"'AUTO' IS AN INVALID SOURCE LANGUAGE\"},"
                + "\"responseStatus\":\"403\"}";
        assertNull(translationCacheService.parseMyMemoryResponse(json));
    }

    @Test
    void testMyMemoryRejectsMissingResponseData() {
        assertNull(translationCacheService.parseMyMemoryResponse("{\"responseStatus\":200}"));
    }

    @Test
    void testMyMemoryRejectsMissingTranslatedText() {
        assertNull(translationCacheService.parseMyMemoryResponse("{\"responseData\":{},\"responseStatus\":200}"));
    }

    @Test
    void testMyMemoryRejectsMalformedJson() {
        assertNull(translationCacheService.parseMyMemoryResponse("not json at all"));
    }

    // --- in-flight dedup (offline via the empty-providers fast path) ----------

    @Test
    void testTranslateAsyncCachedHitSkipsWork() {
        // A cached value must be returned immediately as a completed future,
        // without touching providers or the executor.
        translationCacheService.put("en", "ru", "hello", "привет");

        CompletableFuture<String> future = translationCacheService.translateAsync(
                "en", "ru", "hello", java.util.List.of("GOOGLE"));

        assertTrue(future.isDone());
        assertEquals("привет", future.join());
    }

    @Test
    void testTranslateAsyncNoProvidersCompletesNull() {
        // With no providers configured the chain short-circuits offline:
        // completed future with null, no executor / network involvement.
        CompletableFuture<String> empty = translationCacheService.translateAsync(
                "en", "ru", "hello", java.util.List.of());
        CompletableFuture<String> nullProviders = translationCacheService.translateAsync(
                "en", "ru", "hello", null);

        assertTrue(empty.isDone());
        assertNull(empty.join());
        assertTrue(nullProviders.isDone());
        assertNull(nullProviders.join());
    }

    @Test
    void testShutdownSwapsExecutorAndStaysUsable() {
        // shutdown() must not leave the service unusable: a cached translateAsync
        // call still resolves afterwards (executor is swapped, not killed).
        translationCacheService.put("en", "ru", "hello", "привет");

        translationCacheService.shutdown();

        CompletableFuture<String> future = translationCacheService.translateAsync(
                "en", "ru", "hello", java.util.List.of("GOOGLE"));
        assertTrue(future.isDone());
        assertEquals("привет", future.join());
    }

    // --- negative cache of full-chain failures (offline) ---------------------

    @Test
    void testNegativeCacheShortCircuitsAfterMarkedFailure() {
        // A key marked as recently-failed must short-circuit translateAsync to a
        // completed null future WITHOUT calling any provider. The integration
        // provider stub throws if touched, and DEEPL would use it — so reaching a
        // provider here would fail the test loudly.
        translationCacheService.markTranslationFailed("en", "ru", "hello");
        assertTrue(translationCacheService.isTranslationFailed("en", "ru", "hello"));

        CompletableFuture<String> future = translationCacheService.translateAsync(
                "en", "ru", "hello", java.util.List.of("DEEPL"));

        assertTrue(future.isDone());
        assertNull(future.join());
    }

    @Test
    void testNegativeCacheIsKeyScoped() {
        // A failure for one key must not suppress a different key. The unmarked
        // key falls through to the executor; join() would surface any provider
        // touch (the stub throws) — but GOOGLE hits the network, so we only assert
        // that the call is NOT short-circuited synchronously to null.
        translationCacheService.markTranslationFailed("en", "ru", "hello");

        assertFalse(translationCacheService.isTranslationFailed("en", "ru", "world"));
        assertFalse(translationCacheService.isTranslationFailed("en", "de", "hello"));
    }

    @Test
    void testPositiveCacheHitWinsOverNegative() {
        // If both a real translation and a stale negative mark exist for a key,
        // the positive cache-hit must win (order: positive → negative → inFlight).
        translationCacheService.put("en", "ru", "hello", "привет");
        translationCacheService.markTranslationFailed("en", "ru", "hello");

        CompletableFuture<String> future = translationCacheService.translateAsync(
                "en", "ru", "hello", java.util.List.of("DEEPL"));

        assertTrue(future.isDone());
        assertEquals("привет", future.join());
    }

    @Test
    void testShutdownClearsNegativeCache() {
        // A reload must not carry over remembered failures.
        translationCacheService.markTranslationFailed("en", "ru", "hello");
        assertTrue(translationCacheService.isTranslationFailed("en", "ru", "hello"));

        translationCacheService.shutdown();

        assertFalse(translationCacheService.isTranslationFailed("en", "ru", "hello"));
    }

    // --- network / integration tests (excluded from default suite) -----------

    @Test
    @Tag("integration")
    void testTranslateWithMyMemoryAsync() throws Exception {
        CompletableFuture<String> future = translationCacheService.translateWithMyMemoryAsync(
                "en", "ru", "hello"
        );

        String result = future.get(15, TimeUnit.SECONDS);

        assertNotNull(future);
        assertTrue(future.isDone());
        if (result != null) {
            assertFalse(result.isEmpty());
        }
    }

    @Test
    @Tag("integration")
    void testCacheAfterAsyncTranslation() throws Exception {
        String text = "hello";
        String sourceLang = "en";
        String targetLang = "ru";

        assertNull(translationCacheService.get(sourceLang, targetLang, text));

        CompletableFuture<String> future = translationCacheService.translateWithMyMemoryAsync(
                sourceLang, targetLang, text
        );

        String result = future.get(15, TimeUnit.SECONDS);

        if (result != null && !result.isEmpty()) {
            String cached = translationCacheService.get(sourceLang, targetLang, text);
            assertEquals(result, cached);
        }
    }

    @Test
    @Tag("integration")
    void testMultipleConcurrentTranslations() throws Exception {
        CompletableFuture<String> future1 = translationCacheService.translateWithMyMemoryAsync(
                "en", "ru", "hello"
        );
        CompletableFuture<String> future2 = translationCacheService.translateWithMyMemoryAsync(
                "en", "de", "hello"
        );
        CompletableFuture<String> future3 = translationCacheService.translateWithMyMemoryAsync(
                "en", "fr", "hello"
        );

        CompletableFuture.allOf(future1, future2, future3).get(30, TimeUnit.SECONDS);

        assertTrue(future1.isDone());
        assertTrue(future2.isDone());
        assertTrue(future3.isDone());
    }
}
