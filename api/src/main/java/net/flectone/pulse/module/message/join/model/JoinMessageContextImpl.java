package net.flectone.pulse.module.message.join.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for a player joining the server contexts.
 *
 * @param base the plain context underneath
 * @param playedBefore whether the player has been here before, which picks the first-join wording
 * @param fakeMessage the fakeMessage
 * @param vanished the vanished
 */
@With
@Builder
record JoinMessageContextImpl(
        @NonNull MessageContext base,
        boolean playedBefore,
        boolean fakeMessage,
        boolean vanished
) implements JoinMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new JoinCacheKey(base().createCacheKey(), playedBefore, fakeMessage, vanished);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param playedBefore whether the player has been here before, which picks the first-join wording
     * @param fakeMessage the fakeMessage
     * @param vanished the vanished
     */
    public record JoinCacheKey(
            @NonNull CacheKey base,
            boolean playedBefore,
            boolean fakeMessage,
            boolean vanished
    ) implements CacheKey {
    }

}