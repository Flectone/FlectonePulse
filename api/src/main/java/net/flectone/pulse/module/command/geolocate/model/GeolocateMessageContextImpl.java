package net.flectone.pulse.module.command.geolocate.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for the location looked up for a player's address contexts.
 *
 * @param base the plain context underneath
 * @param response the answer from the geolocation service
 */
@With
@Builder
record GeolocateMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull IpResponse response
) implements GeolocateMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new GeolocateCacheKey(base().createCacheKey(), response);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param response the answer from the geolocation service
     */
    public record GeolocateCacheKey(
            @NonNull CacheKey base,
            @NonNull IpResponse response
    ) implements CacheKey {
    }

}