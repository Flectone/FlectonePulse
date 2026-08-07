package net.flectone.pulse.module.message.chat.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for a message written in one of the configured chats contexts.
 *
 * @param base the plain context underneath
 * @param string the string
 * @param chat the chat it was written in
 */
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

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param playerMessage the playerMessage
     * @param chat the chat it was written in
     */
    public record ChatCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            @NonNull Chat chat
    ) implements CacheKey {
    }

}