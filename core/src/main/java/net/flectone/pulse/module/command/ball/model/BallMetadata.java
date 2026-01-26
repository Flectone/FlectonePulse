package net.flectone.pulse.module.command.ball.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record BallMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        int answer
) implements EventMetadata<L> {}
