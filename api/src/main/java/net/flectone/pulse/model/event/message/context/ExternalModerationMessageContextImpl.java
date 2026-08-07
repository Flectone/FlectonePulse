package net.flectone.pulse.model.event.message.context;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.util.ExternalModeration;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for externalModeration-flavored message contexts.
 *
 * @param base the plain context underneath
 * @param externalModeration the extra value
 */
@With
@Builder
record ExternalModerationMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull ExternalModeration externalModeration
) implements ExternalModerationMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new ExternalModerationCacheKey(base().createCacheKey(), externalModeration);
    }

    /**
     * Cache key that mixes the extra value into the base key.
     *
     * @param base the base key
     * @param externalModeration the extra value
     */
    public record ExternalModerationCacheKey(
            @NonNull CacheKey base,
            @NonNull ExternalModeration externalModeration
    ) implements CacheKey {
    }

}