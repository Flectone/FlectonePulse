package net.flectone.pulse.module.command.minesweeper.model;

import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * The board of a minesweeper game.
 * Builds on {@link MessageContext}.
 * @author TheFaser
 */
public interface MinesweeperMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static MinesweeperMessageContextImpl.MinesweeperMessageContextImplBuilder builder() {
        return MinesweeperMessageContextImpl.builder();
    }

    /**
     * The current board.
     *
     * @return the current board
     */
    @NonNull Minesweeper minesweeper();

    /**
     * Returns a copy carrying a different value.
     *
     * @param minesweeper the current board
     * @return the copy
     */
    MinesweeperMessageContext withMinesweeper(@NonNull Minesweeper minesweeper);

}