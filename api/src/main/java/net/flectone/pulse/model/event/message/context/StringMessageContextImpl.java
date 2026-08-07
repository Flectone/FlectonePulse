package net.flectone.pulse.model.event.message.context;

import lombok.Builder;
import lombok.With;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for string-flavored message contexts.
 *
 * @param base the plain context underneath
 * @param string the extra value
 */
@With
@Builder
record StringMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String string
) implements StringMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new StringCacheKey(base().createCacheKey(), string);
    }

    /**
     * Cache key that mixes the extra value into the base key.
     *
     * @param base the base key
     * @param string the extra value
     */
    public record StringCacheKey(
            @NonNull CacheKey base,
            @NonNull String string
    ) implements CacheKey {
    }

}
