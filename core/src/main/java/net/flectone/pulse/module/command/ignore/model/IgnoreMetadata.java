package net.flectone.pulse.module.command.ignore.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record IgnoreMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull Ignore ignore,
        boolean ignored
) implements EventMetadata<L> {}
