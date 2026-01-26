package net.flectone.pulse.module.command.rockpaperscissors.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.command.rockpaperscissors.RockpaperscissorsModule;
import org.jspecify.annotations.NonNull;

@Builder
public record RockPaperScissorsMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull RockPaperScissors rockPaperScissors,
        RockpaperscissorsModule.@NonNull GamePhase gamePhase
) implements EventMetadata<L> {}
