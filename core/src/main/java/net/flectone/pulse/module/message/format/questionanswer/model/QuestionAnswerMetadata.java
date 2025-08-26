package net.flectone.pulse.module.message.format.questionanswer.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.event.EventMetadata;

@Getter
@SuperBuilder
public class QuestionAnswerMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final String question;

}
