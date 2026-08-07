package net.flectone.pulse.module.command.tictactoe.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.tictactoe.TictactoeModule;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for a round of tic-tac-toe contexts.
 *
 * @param base the plain context underneath
 * @param ticTacToe the game
 * @param gamePhase how far the round has got
 */
@With
@Builder
record TicTacToeMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull TicTacToe ticTacToe,
        TictactoeModule.@NonNull GamePhase gamePhase
) implements TicTacToeMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new TicTacToeCacheKey(base().createCacheKey(), ticTacToe, gamePhase);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param ticTacToe the game
     * @param gamePhase how far the round has got
     */
    public record TicTacToeCacheKey(
            @NonNull CacheKey base,
            @NonNull TicTacToe ticTacToe,
            TictactoeModule.@NonNull GamePhase gamePhase
    ) implements CacheKey {
    }

}