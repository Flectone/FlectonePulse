package net.flectone.pulse.platform.registry;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.CacheName;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.incendo.cloud.type.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CacheRegistry {

    private final Map<CacheName, Cache<?, ?>> cacheMap = new EnumMap<>(CacheName.class);

    private final FileResolver fileResolver;

    public void init() {
        Arrays.stream(CacheName.values()).forEach(this::create);
    }

    public <K, V> void create(CacheName cacheName) {
        if (cacheMap.containsKey(cacheName)) {
            throw new IllegalArgumentException("Cache already created for " + cacheName);
        }

        Config.Cache.CacheSetting cacheSetting = fileResolver.getConfig()
                .getCache()
                .getTypes()
                .get(cacheName);

        if (cacheSetting == null) {
            throw new IllegalArgumentException("No cache setting for " + cacheName);
        }

        Cache<K, V> cache = CacheBuilder.newBuilder()
                .expireAfterAccess(cacheSetting.getDuration(), cacheSetting.getTimeUnit())
                .maximumSize(cacheSetting.getSize())
                .build();

        cacheMap.put(cacheName, cache);
    }

    public Cache<UUID, AtomicInteger> getDialogClickCache() {
        return getCache(CacheName.DIALOG_CLICK);
    }

    public Cache<UUID, FPlayer> getOfflinePlayersCache() {
        return getCache(CacheName.OFFLINE_PLAYERS);
    }

    public Cache<Pair<UUID, Moderation.Type>, List<Moderation>> getModerationCache() {
        return getCache(CacheName.MODERATION);
    }

    public Cache<String, String> getLegacyColorMessageCache() {
        return getCache(CacheName.LEGACY_COLOR_MESSAGE);
    }

    public Cache<String, String> getMentionMessageCache() {
        return getCache(CacheName.MENTION_MESSAGE);
    }

    public Cache<String, String> getSwearMessageCache() {
        return getCache(CacheName.SWEAR_MESSAGE);
    }

    public Cache<String, String> getReplacementMessageCache() {
        return getCache(CacheName.REPLACEMENT_MESSAGE);
    }

    public Cache<String, Component> getReplacementImageCache() {
        return getCache(CacheName.REPLACEMENT_IMAGE);
    }

    public Cache<String, UUID> getTranslateMessageCache() {
        return getCache(CacheName.TRANSLATE_MESSAGE);
    }

    public Cache<UUID, PlayerHeadObjectContents.ProfileProperty> getProfilePropertyCache() {
        return getCache(CacheName.PROFILE_PROPERTY);
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
