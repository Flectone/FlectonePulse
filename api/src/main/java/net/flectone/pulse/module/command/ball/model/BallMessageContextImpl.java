package net.flectone.pulse.module.command.ball.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for the reply of the magic-ball command contexts.
 *
 * @param base the plain context underneath
 * @param string the string
 * @param answer index of the chosen answer in the localization list
 */
@With
@Builder
record BallMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String string,
        int answer
) implements BallMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new BallCacheKey(base().createCacheKey(), string, answer);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param playerMessage the playerMessage
     * @param answer index of the chosen answer in the localization list
     */
    public record BallCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            int answer
    ) implements CacheKey {
    }

}