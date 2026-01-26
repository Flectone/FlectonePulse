package net.flectone.pulse.module.message.chat.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record ChatMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull Chat chat
) implements EventMetadata<L> {}
