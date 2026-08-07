package net.flectone.pulse.module.command.tictactoe.model;

import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.tictactoe.TictactoeModule;
import org.jspecify.annotations.NonNull;

/**
 * A round of tic-tac-toe.
 * Builds on {@link MessageContext}.
 * @author TheFaser
 */
public interface TicTacToeMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static TicTacToeMessageContextImpl.TicTacToeMessageContextImplBuilder builder() {
        return TicTacToeMessageContextImpl.builder();
    }

    /**
     * The game.
     *
     * @return the game
     */
    @NonNull TicTacToe ticTacToe();

    /**
     * How far the round has got.
     *
     * @return how far the round has got
     */
    TictactoeModule.@NonNull GamePhase gamePhase();

    /**
     * Returns a copy carrying a different value.
     *
     * @param ticTacToe the game
     * @return the copy
     */
    TicTacToeMessageContext withTicTacToe(@NonNull TicTacToe ticTacToe);

    /**
     * Returns a copy carrying a different value.
     *
     * @param gamePhase how far the round has got
     * @return the copy
     */
    TicTacToeMessageContext withGamePhase(TictactoeModule.@NonNull GamePhase gamePhase);

}