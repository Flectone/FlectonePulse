package net.flectone.pulse.module.message.format.questionanswer.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record QuestionAnswerMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull String question
) implements EventMetadata<L> {}
