package net.flectone.pulse.module.command.minesweeper.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

@With
@Builder
record MinesweeperMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull Minesweeper minesweeper
) implements MinesweeperMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new MinesweeperCacheKey(base().createCacheKey(), minesweeper);
    }

    public record MinesweeperCacheKey(
            @NonNull CacheKey base,
            @NonNull Minesweeper minesweeper
    ) implements CacheKey {
    }

}