package net.flectone.pulse.module.message.format.questionanswer.model;

import org.jspecify.annotations.NonNull;
import net.flectone.pulse.model.event.message.context.StringMessageContext;

public interface QuestionAnswerMessageContext extends StringMessageContext {

    static QuestionAnswerMessageContextImpl.QuestionAnswerMessageContextImplBuilder builder() {
        return QuestionAnswerMessageContextImpl.builder();
    }

    @NonNull String question();

    QuestionAnswerMessageContext withQuestion(@NonNull String question);

}