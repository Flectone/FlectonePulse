package net.flectone.pulse.module.command.rockpaperscissors.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.rockpaperscissors.RockpaperscissorsModule;
import org.jspecify.annotations.NonNull;

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

    public record RockPaperScissorsCacheKey(
            @NonNull CacheKey base,
            @NonNull RockPaperScissors rockPaperScissors,
            RockpaperscissorsModule.@NonNull GamePhase gamePhase
    ) implements CacheKey {
    }

}