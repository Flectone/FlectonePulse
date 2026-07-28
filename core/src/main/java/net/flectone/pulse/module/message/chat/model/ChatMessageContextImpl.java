package net.flectone.pulse.module.message.chat.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

@With
@Builder
record ChatMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String string,
        @NonNull Chat chat
) implements ChatMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new ChatCacheKey(base().createCacheKey(), string, chat);
    }

    public record ChatCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            @NonNull Chat chat
    ) implements CacheKey {
    }

}