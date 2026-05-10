package net.flectone.pulse.module.command.ball.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record BallMetadata<L extends LocalizationSetting>(
        @NonNull BaseEventMetadata<L> base,
        int answer
) implements EventMetadata<L> {
}
