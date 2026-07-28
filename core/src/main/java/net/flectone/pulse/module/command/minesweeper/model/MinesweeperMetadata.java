package net.flectone.pulse.module.command.minesweeper.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record MinesweeperMetadata(
        @NonNull BaseEventMetadata base,
        @NonNull Minesweeper minesweeper
) implements EventMetadata {
}
