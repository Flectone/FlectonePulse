package net.flectone.pulse.module.integration.discord.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Carrier for a message that came from Discord contexts.
 *
 * @param base the plain context underneath
 * @param string the string
 * @param globalName the author's Discord display name
 * @param nickname the author's nickname on the server
 * @param displayName the name shown for the author
 * @param userName the author's Discord handle
 * @param reply the author and text of the replied-to message, or null
 */
@With
@Builder
record DiscordMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String string,
        @NonNull String globalName,
        @NonNull String nickname,
        @NonNull String displayName,
        @NonNull String userName,
        @Nullable Pair<String, String> reply
) implements DiscordMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new DiscordCacheKey(base().createCacheKey(), string, globalName, nickname, displayName, userName, reply);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param playerMessage the playerMessage
     * @param globalName the author's Discord display name
     * @param nickname the author's nickname on the server
     * @param displayName the name shown for the author
     * @param userName the author's Discord handle
     * @param reply the author and text of the replied-to message, or null
     */
    public record DiscordCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            @NonNull String globalName,
            @NonNull String nickname,
            @NonNull String displayName,
            @NonNull String userName,
            @Nullable Pair<String, String> reply
    ) implements CacheKey {
    }

}