package net.flectone.pulse.module.command.online.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for the answer of the online command contexts.
 *
 * @param base the plain context underneath
 * @param type which player count was asked for
 */
@With
@Builder
record OnlineMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String type
) implements OnlineMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new OnlineCacheKey(base().createCacheKey(), type);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param type which player count was asked for
     */
    public record OnlineCacheKey(
            @NonNull CacheKey base,
            @NonNull String type
    ) implements CacheKey {
    }

}