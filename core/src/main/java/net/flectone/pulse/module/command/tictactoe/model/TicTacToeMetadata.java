package net.flectone.pulse.module.command.tictactoe.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.command.tictactoe.TictactoeModule;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record TicTacToeMetadata(
        @NonNull BaseEventMetadata base,
        @NonNull TicTacToe ticTacToe,
        TictactoeModule.@NonNull GamePhase gamePhase
) implements EventMetadata {
}
