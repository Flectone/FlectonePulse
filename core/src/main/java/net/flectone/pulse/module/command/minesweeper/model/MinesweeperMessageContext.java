package net.flectone.pulse.module.command.minesweeper.model;

import org.jspecify.annotations.NonNull;
import net.flectone.pulse.model.event.message.context.MessageContext;

public interface MinesweeperMessageContext extends MessageContext {

    static MinesweeperMessageContextImpl.MinesweeperMessageContextImplBuilder builder() {
        return MinesweeperMessageContextImpl.builder();
    }

    @NonNull Minesweeper minesweeper();

    MinesweeperMessageContext withMinesweeper(@NonNull Minesweeper minesweeper);

}