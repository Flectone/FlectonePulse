package net.flectone.pulse.module.command.dice.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Carrier for the outcome of a dice roll contexts.
 *
 * @param base the plain context underneath
 * @param cubes the value rolled on each die
 */
@With
@Builder
record DiceMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull List<Integer> cubes
) implements DiceMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new DiceCacheKey(base().createCacheKey(), cubes);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param cubes the value rolled on each die
     */
    public record DiceCacheKey(
            @NonNull CacheKey base,
            @NonNull List<Integer> cubes
    ) implements CacheKey {
    }

}