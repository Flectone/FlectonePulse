package net.flectone.pulse.module.command.minesweeper.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for the board of a minesweeper game contexts.
 *
 * @param base the plain context underneath
 * @param minesweeper the current board
 */
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

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param minesweeper the current board
     */
    public record MinesweeperCacheKey(
            @NonNull CacheKey base,
            @NonNull Minesweeper minesweeper
    ) implements CacheKey {
    }

}