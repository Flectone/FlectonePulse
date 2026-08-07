package net.flectone.pulse.module.command.spy.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for a change to a moderator's spy mode, or a message it caught contexts.
 *
 * @param base the plain context underneath
 * @param string the string
 * @param turned whether spy mode was switched on, rather than off
 * @param action what was spied on
 */
@With
@Builder
record SpyMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String string,
        boolean turned,
        @NonNull String action
) implements SpyMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new SpyCacheKey(base().createCacheKey(), string, turned, action);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param playerMessage the playerMessage
     * @param turned whether spy mode was switched on, rather than off
     * @param action what was spied on
     */
    public record SpyCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            boolean turned,
            @NonNull String action
    ) implements CacheKey {
    }

}