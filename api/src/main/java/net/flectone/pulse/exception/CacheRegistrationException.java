package net.flectone.pulse.exception;

import net.flectone.pulse.constant.CacheName;

/**
 * Thrown when a cache is created twice, or asked for before it has been created.
 *
 * @author TheFaser
 */
public class CacheRegistrationException extends RuntimeException {

    /**
     * Creates the exception, naming the cache the problem is about.
     *
     * @param message what went wrong
     * @param cacheName the cache
     */
    public CacheRegistrationException(String message, CacheName cacheName) {
        super(message + ": " + cacheName);
    }

}
