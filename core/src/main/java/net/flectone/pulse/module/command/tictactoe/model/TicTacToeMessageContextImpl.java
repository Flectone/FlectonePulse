package net.flectone.pulse.module.command.tictactoe.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.tictactoe.TictactoeModule;
import org.jspecify.annotations.NonNull;

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

    public record TicTacToeCacheKey(
            @NonNull CacheKey base,
            @NonNull TicTacToe ticTacToe,
            TictactoeModule.@NonNull GamePhase gamePhase
    ) implements CacheKey {
    }

}