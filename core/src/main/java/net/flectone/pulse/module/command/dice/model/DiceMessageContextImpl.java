package net.flectone.pulse.module.command.dice.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

import java.util.List;

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

    public record DiceCacheKey(
            @NonNull CacheKey base,
            @NonNull List<Integer> cubes
    ) implements CacheKey {
    }

}