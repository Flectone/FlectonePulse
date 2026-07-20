package net.flectone.pulse.platform.registry;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.util.constant.CacheName;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

@Singleton
public class CacheRegistry {

    private final Map<CacheName, Cache<?, ?>> cacheMap = new EnumMap<>(CacheName.class);

    private final FileFacade fileFacade;

    @Inject
    public CacheRegistry(FileFacade fileFacade) {
        this.fileFacade = fileFacade;

        init();
    }

    public void init() {
        Arrays.stream(CacheName.values()).forEach(this::create);
    }

    public void invalidate() {
        cacheMap.keySet().forEach(this::invalidate);
    }

    public void create(CacheName cacheName) {
        if (cacheMap.containsKey(cacheName)) {
            throw new IllegalArgumentException("Cache already created for " + cacheName);
        }

        Config.Cache.CacheSetting cacheSetting = config(cacheName);
        Cache<?, ?> cache = cacheSetting.expireAfterWrite()
                ? CacheBuilder.newBuilder()
                  .expireAfterWrite(cacheSetting.duration(), cacheSetting.timeUnit())
                  .maximumSize(cacheSetting.size())
                  .build()
                : CacheBuilder.newBuilder()
                  .expireAfterAccess(cacheSetting.duration(), cacheSetting.timeUnit())
                  .maximumSize(cacheSetting.size())
                  .build();

        cacheMap.put(cacheName, cache);
    }

    public boolean invalidate(CacheName cacheName) {
        Config.Cache.CacheSetting cacheSetting = config(cacheName);
        if (!cacheSetting.invalidateOnReload()) return false;

        getCache(cacheName).invalidateAll();
        return true;
    }

    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(CacheName cacheName) {
        Object cache = cacheMap.get(cacheName);
        if (cache == null) {
            throw new IllegalArgumentException("No cache created for " + cacheName);
        }

        return (Cache<K, V>) cache;
    }

    private Config.Cache.@NonNull CacheSetting config(CacheName cacheName) {
        Config.Cache.CacheSetting cacheSetting = fileFacade.config()
                .cache()
                .types()
                .get(cacheName);

        if (cacheSetting == null) {
            throw new IllegalArgumentException("No cache setting for " + cacheName);
        }

        return cacheSetting;
    }
}
