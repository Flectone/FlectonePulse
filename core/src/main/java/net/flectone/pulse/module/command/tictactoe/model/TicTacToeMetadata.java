package net.flectone.pulse.module.command.tictactoe.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.command.tictactoe.TictactoeModule;
import org.jspecify.annotations.NonNull;

@Getter
@SuperBuilder
public class TicTacToeMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    @NonNull
    private final TicTacToe ticTacToe;

    private final TictactoeModule.@NonNull GamePhase gamePhase;

}
