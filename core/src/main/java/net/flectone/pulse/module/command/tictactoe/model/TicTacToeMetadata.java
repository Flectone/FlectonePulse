package net.flectone.pulse.module.command.tictactoe.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.command.tictactoe.TictactoeModule;

@Getter
@SuperBuilder
public class TicTacToeMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final TicTacToe ticTacToe;

    @NonNull
    private final TictactoeModule.GamePhase gamePhase;

}
