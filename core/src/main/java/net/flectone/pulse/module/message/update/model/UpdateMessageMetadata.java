package net.flectone.pulse.module.message.update.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record UpdateMessageMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull String latestVersion,
        @NonNull String currentVersion
) implements EventMetadata<L> {}
