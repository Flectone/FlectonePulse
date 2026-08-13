package net.flectone.pulse.platform.registry;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.constant.CacheName;
import net.flectone.pulse.exception.CacheRegistrationException;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
public class CacheRegistryImpl implements CacheRegistry  {

    private final Map<CacheName, Cache<?, ?>> cacheMap = new EnumMap<>(CacheName.class);
    private final Map<CacheName, List<RemovalListener<Object, Object>>> removalListeners = new EnumMap<>(CacheName.class);

    private final FileFacade fileFacade;
    private final FLogger fLogger;

    @Inject
    public CacheRegistryImpl(FileFacade fileFacade, FLogger fLogger) {
        this.fileFacade = fileFacade;
        this.fLogger = fLogger;

        for (CacheName cacheName : CacheName.values()) {
            removalListeners.put(cacheName, new CopyOnWriteArrayList<>());
        }

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
            throw new CacheRegistrationException("Cache already created", cacheName);
        }

        Config.Cache.CacheSetting setting = config(cacheName);

        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder()
                .maximumSize(setting.size())
                .removalListener(dispatcher(cacheName));

        cacheMap.put(cacheName, (setting.expireAfterWrite()
                ? builder.expireAfterWrite(setting.duration(), setting.timeUnit())
                : builder.expireAfterAccess(setting.duration(), setting.timeUnit())
        ).build());
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
            throw new CacheRegistrationException("No cache created", cacheName);
        }

        return (Cache<K, V>) cache;
    }

    @SuppressWarnings("unchecked")
    public <K, V> void addRemovalListener(CacheName cacheName, RemovalListener<K, V> listener) {
        removalListeners.get(cacheName).add((RemovalListener<Object, Object>) listener);
    }

    private Config.Cache.@NonNull CacheSetting config(CacheName cacheName) {
        Config.Cache.CacheSetting cacheSetting = fileFacade.config()
                .cache()
                .types()
                .get(cacheName);

        if (cacheSetting == null) {
            throw new CacheRegistrationException("No cache setting", cacheName);
        }

        return cacheSetting;
    }

    private RemovalListener<Object, Object> dispatcher(CacheName cacheName) {
        return notification -> {
            if (!notification.wasEvicted()) return;

            for (RemovalListener<Object, Object> listener : removalListeners.get(cacheName)) {
                try {
                    listener.onRemoval(notification);
                } catch (Exception e) {
                    fLogger.warning("Removal listener failed for cache " + cacheName, e);
                }
            }
        };
    }

}
