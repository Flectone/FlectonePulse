package net.flectone.pulse.module.message.format.questionanswer.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;
import org.jspecify.annotations.NonNull;

/**
 * An automatic reply to a recognized question.
 * Builds on {@link StringMessageContext}.
 * @author TheFaser
 */
public interface QuestionAnswerMessageContext extends StringMessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static QuestionAnswerMessageContextImpl.QuestionAnswerMessageContextImplBuilder builder() {
        return QuestionAnswerMessageContextImpl.builder();
    }

    /**
     * The question that was matched.
     *
     * @return the question that was matched
     */
    @NonNull String question();

    /**
     * Returns a copy carrying a different value.
     *
     * @param question the question that was matched
     * @return the copy
     */
    QuestionAnswerMessageContext withQuestion(@NonNull String question);

}