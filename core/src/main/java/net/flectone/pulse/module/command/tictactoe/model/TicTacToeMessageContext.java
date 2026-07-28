package net.flectone.pulse.module.command.tictactoe.model;

import org.jspecify.annotations.NonNull;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.tictactoe.TictactoeModule;

public interface TicTacToeMessageContext extends MessageContext {

    static TicTacToeMessageContextImpl.TicTacToeMessageContextImplBuilder builder() {
        return TicTacToeMessageContextImpl.builder();
    }

    @NonNull TicTacToe ticTacToe();

    TictactoeModule.@NonNull GamePhase gamePhase();

    TicTacToeMessageContext withTicTacToe(@NonNull TicTacToe ticTacToe);

    TicTacToeMessageContext withGamePhase(TictactoeModule.@NonNull GamePhase gamePhase);

}