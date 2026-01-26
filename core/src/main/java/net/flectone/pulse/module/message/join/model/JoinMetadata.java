package net.flectone.pulse.module.message.join.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record JoinMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        boolean playedBefore,
        boolean ignoreVanish
) implements EventMetadata<L> {}
