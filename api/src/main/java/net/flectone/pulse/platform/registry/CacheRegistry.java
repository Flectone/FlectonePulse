package net.flectone.pulse.platform.registry;

import net.flectone.pulse.constant.CacheName;

/**
 * Owns the plugin's caches, so each can be built from config and cleared on reload.
 * @author TheFaser
 */
public interface CacheRegistry {

    /**
     * Builds every cache from the sizes and lifetimes in the config.
     */
    void init();

    /**
     * Empties every cache.
     */
    void invalidate();

    /**
     * Builds one cache, replacing it if it already exists.
     *
     * @param cacheName which cache to build
     */
    void create(CacheName cacheName);

    /**
     * Empties one cache.
     *
     * @param cacheName which cache to empty
     * @return true if the cache existed
     */
    boolean invalidate(CacheName cacheName);

}
