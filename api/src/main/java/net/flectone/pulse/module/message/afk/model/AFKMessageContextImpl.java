package net.flectone.pulse.module.message.afk.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for a change to a player's afk state contexts.
 *
 * @param base the plain context underneath
 * @param newStatus whether the player just went afk, rather than came back
 * @param fakeMessage the fakeMessage
 * @param vanished the vanished
 */
@With
@Builder
record AFKMessageContextImpl(
        @NonNull MessageContext base,
        boolean newStatus,
        boolean fakeMessage,
        boolean vanished
) implements AFKMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new AFKCacheKey(base().createCacheKey(), newStatus, fakeMessage, vanished);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param newStatus whether the player just went afk, rather than came back
     * @param fakeMessage the fakeMessage
     * @param vanished the vanished
     */
    public record AFKCacheKey(
            @NonNull CacheKey base,
            boolean newStatus,
            boolean fakeMessage,
            boolean vanished
    ) implements CacheKey {
    }

}