package net.flectone.pulse.platform.registry;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.util.constant.CacheName;
import net.flectone.pulse.util.file.FileFacade;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CacheRegistry {

    private final Map<CacheName, Cache<?, ?>> cacheMap = new EnumMap<>(CacheName.class);

    private final FileFacade fileFacade;

    public void init() {
        Arrays.stream(CacheName.values()).forEach(this::create);
    }

    public void invalidate() {
        cacheMap.keySet().forEach(this::invalidate);
    }

    public <K, V> void create(CacheName cacheName) {
        if (cacheMap.containsKey(cacheName)) {
            throw new IllegalArgumentException("Cache already created for " + cacheName);
        }

        Config.Cache.CacheSetting cacheSetting = fileFacade.config()
                .cache()
                .types()
                .get(cacheName);

        if (cacheSetting == null) {
            throw new IllegalArgumentException("No cache setting for " + cacheName);
        }

        Cache<K, V> cache = CacheBuilder.newBuilder()
                .expireAfterAccess(cacheSetting.duration(), cacheSetting.timeUnit())
                .maximumSize(cacheSetting.size())
                .build();

        cacheMap.put(cacheName, cache);
    }

    public void invalidate(CacheName cacheName) {
        if (!cacheMap.containsKey(cacheName)) return;

        Config.Cache.CacheSetting cacheSetting = fileFacade.config()
                .cache()
                .types()
                .get(cacheName);

        if (cacheSetting == null) {
            throw new IllegalArgumentException("No cache setting for " + cacheName);
        }

        if (cacheSetting.invalidateOnReload()) {
            cacheMap.get(cacheName).invalidateAll();
        }
    }

    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(CacheName cacheName) {
        Object cache = cacheMap.get(cacheName);
        if (cache == null) {
            throw new IllegalArgumentException("No cache created for " + cacheName);
        }

        return (Cache<K, V>) cache;
    }
}
