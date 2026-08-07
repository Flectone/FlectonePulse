package net.flectone.pulse.module.command.try_.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for the outcome of a try roll contexts.
 *
 * @param base the plain context underneath
 * @param string the string
 * @param percent the rolled percentage, which decides success or failure
 */
@With
@Builder
record TryMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String string,
        int percent
) implements TryMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new TryCacheKey(base().createCacheKey(), string, percent);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param playerMessage the playerMessage
     * @param percent the rolled percentage, which decides success or failure
     */
    public record TryCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            int percent
    ) implements CacheKey {
    }

}