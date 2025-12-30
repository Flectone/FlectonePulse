package net.flectone.pulse.module.message.format.questionanswer.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Getter
@SuperBuilder
public class QuestionAnswerMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    @NonNull
    private final String question;

}
