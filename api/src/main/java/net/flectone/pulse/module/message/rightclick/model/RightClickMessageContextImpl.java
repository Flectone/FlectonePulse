package net.flectone.pulse.module.message.rightclick.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for one player right-clicking another contexts.
 *
 * @param base the plain context underneath
 * @param target the player who was clicked
 */
@With
@Builder
record RightClickMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull FPlayer target
) implements RightClickMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new RightClickCacheKey(base().createCacheKey(), target);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param target the player who was clicked
     */
    public record RightClickCacheKey(
            @NonNull CacheKey base,
            @NonNull FPlayer target
    ) implements CacheKey {
    }

}