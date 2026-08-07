package net.flectone.pulse.module.command.ignore.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for a change to a player's ignore list contexts.
 *
 * @param base the plain context underneath
 * @param ignore the ignore entry
 * @param ignored whether the target is now ignored, rather than un-ignored
 */
@With
@Builder
record IgnoreMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull Ignore ignore,
        boolean ignored
) implements IgnoreMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new IgnoreCacheKey(base().createCacheKey(), ignore, ignored);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param ignore the ignore entry
     * @param ignored whether the target is now ignored, rather than un-ignored
     */
    public record IgnoreCacheKey(
            @NonNull CacheKey base,
            @NonNull Ignore ignore,
            boolean ignored
    ) implements CacheKey {
    }

}