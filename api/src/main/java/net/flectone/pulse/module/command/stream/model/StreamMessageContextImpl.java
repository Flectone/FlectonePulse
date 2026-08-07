package net.flectone.pulse.module.command.stream.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Carrier for a change to a player's stream status contexts.
 *
 * @param base the plain context underneath
 * @param turned whether streaming was announced, rather than ended
 * @param urls the stream links, or null if none were given
 */
@With
@Builder
record StreamMessageContextImpl(
        @NonNull MessageContext base,
        boolean turned,
        @Nullable String urls
) implements StreamMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new StreamCacheKey(base().createCacheKey(), turned, urls);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param turned whether streaming was announced, rather than ended
     * @param urls the stream links, or null if none were given
     */
    public record StreamCacheKey(
            @NonNull CacheKey base,
            boolean turned,
            @Nullable String urls
    ) implements CacheKey {
    }

}