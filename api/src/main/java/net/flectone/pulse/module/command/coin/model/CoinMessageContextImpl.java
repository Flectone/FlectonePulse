package net.flectone.pulse.module.command.coin.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for the outcome of a coin flip contexts.
 *
 * @param base the plain context underneath
 * @param percent the rolled percentage, which decides heads, tails or edge
 */
@With
@Builder
record CoinMessageContextImpl(
        @NonNull MessageContext base,
        int percent
) implements CoinMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new CoinCacheKey(base().createCacheKey(), percent);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param percent the rolled percentage, which decides heads, tails or edge
     */
    public record CoinCacheKey(
            @NonNull CacheKey base,
            int percent
    ) implements CacheKey {
    }

}