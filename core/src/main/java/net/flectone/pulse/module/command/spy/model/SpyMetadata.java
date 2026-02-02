package net.flectone.pulse.module.command.spy.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record SpyMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        boolean turned,
        @NonNull String action
) implements EventMetadata<L> {}
