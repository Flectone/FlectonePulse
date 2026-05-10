package net.flectone.pulse.module.message.format.questionanswer.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record QuestionAnswerMetadata<L extends LocalizationSetting>(
        @NonNull BaseEventMetadata<L> base,
        @NonNull String question
) implements EventMetadata<L> {
}
