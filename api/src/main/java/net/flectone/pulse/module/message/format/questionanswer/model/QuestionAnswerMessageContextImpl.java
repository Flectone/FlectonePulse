package net.flectone.pulse.module.message.format.questionanswer.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for an automatic reply to a recognized question contexts.
 *
 * @param base the plain context underneath
 * @param string the string
 * @param question the question that was matched
 */
@With
@Builder
record QuestionAnswerMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String string,
        @NonNull String question
) implements QuestionAnswerMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new QuestionAnswerCacheKey(base().createCacheKey(), string, question);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param playerMessage the playerMessage
     * @param question the question that was matched
     */
    public record QuestionAnswerCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            @NonNull String question
    ) implements CacheKey {
    }

}