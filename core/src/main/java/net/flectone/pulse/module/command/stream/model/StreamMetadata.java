package net.flectone.pulse.module.command.stream.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Builder
public record StreamMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        boolean turned,
        @Nullable String urls
) implements EventMetadata<L> {}
