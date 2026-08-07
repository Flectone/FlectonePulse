package net.flectone.pulse.module.integration.telegram.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Carrier for a message that came from Telegram contexts.
 *
 * @param base the plain context underneath
 * @param string the string
 * @param userName the author's Telegram handle
 * @param firstName the author's first name
 * @param lastName the author's last name
 * @param chat the chat the message was posted in
 * @param reply the author and text of the replied-to message, or null
 */
@With
@Builder
record TelegramMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String string,
        @NonNull String userName,
        @NonNull String firstName,
        @NonNull String lastName,
        @NonNull String chat,
        @Nullable Pair<String, String> reply
) implements TelegramMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new TelegramCacheKey(base().createCacheKey(), string, userName, firstName, lastName, chat, reply);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param playerMessage the playerMessage
     * @param userName the author's Telegram handle
     * @param firstName the author's first name
     * @param lastName the author's last name
     * @param chat the chat the message was posted in
     * @param reply the author and text of the replied-to message, or null
     */
    public record TelegramCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            @NonNull String userName,
            @NonNull String firstName,
            @NonNull String lastName,
            @NonNull String chat,
            @Nullable Pair<String, String> reply
    ) implements CacheKey {

    }
}