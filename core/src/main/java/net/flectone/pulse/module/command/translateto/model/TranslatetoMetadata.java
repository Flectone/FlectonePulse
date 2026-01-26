package net.flectone.pulse.module.command.translateto.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record TranslatetoMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull String targetLanguage,
        @NonNull String messageToTranslate
) implements EventMetadata<L> {}
