package net.flectone.pulse.module.command.minesweeper.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record MinesweeperMetadata<L extends LocalizationSetting>(
        @NonNull BaseEventMetadata<L> base,
        @NonNull Minesweeper minesweeper
) implements EventMetadata<L> {
}
