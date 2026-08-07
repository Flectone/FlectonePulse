package net.flectone.pulse.module.command.rockpaperscissors.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.rockpaperscissors.RockpaperscissorsModule;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for a round of rock-paper-scissors contexts.
 *
 * @param base the plain context underneath
 * @param rockPaperScissors the game
 * @param gamePhase how far the round has got
 */
@With
@Builder
record RockPaperScissorsMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull RockPaperScissors rockPaperScissors,
        RockpaperscissorsModule.@NonNull GamePhase gamePhase
) implements RockPaperScissorsMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new RockPaperScissorsCacheKey(base().createCacheKey(), rockPaperScissors, gamePhase);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param rockPaperScissors the game
     * @param gamePhase how far the round has got
     */
    public record RockPaperScissorsCacheKey(
            @NonNull CacheKey base,
            @NonNull RockPaperScissors rockPaperScissors,
            RockpaperscissorsModule.@NonNull GamePhase gamePhase
    ) implements CacheKey {
    }

}