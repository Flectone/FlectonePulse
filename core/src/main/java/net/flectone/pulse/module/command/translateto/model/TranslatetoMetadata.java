package net.flectone.pulse.module.command.translateto.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record TranslatetoMetadata(
        @NonNull BaseEventMetadata base,
        @NonNull String targetLanguage,
        @NonNull String messageToTranslate
) implements EventMetadata {
}
