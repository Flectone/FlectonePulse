package net.flectone.pulse.module.command.tell.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for a private-message context.
 *
 * @param base the plain context underneath
 * @param string the message text
 * @param target the player receiving the message
 */
@With
@Builder
record TellMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String string,
        @NonNull FPlayer target
) implements TellMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new TellCacheKey(base().createCacheKey(), string, target);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param string the message text
     * @param target the target player
     */
    public record TellCacheKey(
            @NonNull CacheKey base,
            @NonNull String string,
            @NonNull FPlayer target
    ) implements CacheKey {
    }

}