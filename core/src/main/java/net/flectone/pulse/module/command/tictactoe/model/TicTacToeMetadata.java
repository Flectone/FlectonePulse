package net.flectone.pulse.module.command.tictactoe.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.command.tictactoe.TictactoeModule;
import org.jspecify.annotations.NonNull;

@Builder
public record TicTacToeMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull TicTacToe ticTacToe,
        TictactoeModule.@NonNull GamePhase gamePhase
) implements EventMetadata<L> {}
