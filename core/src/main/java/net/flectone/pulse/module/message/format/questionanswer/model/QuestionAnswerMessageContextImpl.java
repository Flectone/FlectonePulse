package net.flectone.pulse.module.message.format.questionanswer.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

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

    public record QuestionAnswerCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            @NonNull String question
    ) implements CacheKey {
    }

}