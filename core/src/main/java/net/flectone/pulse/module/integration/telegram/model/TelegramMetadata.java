package net.flectone.pulse.module.integration.telegram.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record TelegramMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull String userName,
        @NonNull String firstName,
        @NonNull String lastName,
        @NonNull String chat
) implements EventMetadata<L> {}
