package net.flectone.pulse.model.event.message.context;

import lombok.Builder;
import lombok.With;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for component-flavored message contexts.
 *
 * @param base the plain context underneath
 * @param component the extra value
 */
@With
@Builder
record ComponentMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull Component component
) implements ComponentMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new ComponentCacheKey(base().createCacheKey(), component);
    }

    /**
     * Cache key that mixes the extra value into the base key.
     *
     * @param base the base key
     * @param component the extra value
     */
    public record ComponentCacheKey(
            @NonNull CacheKey base,
            @NonNull Component component
    ) implements CacheKey {
    }

}