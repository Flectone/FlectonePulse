package net.flectone.pulse.module.integration.twitch.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Carrier for a message that came from Twitch chat contexts.
 *
 * @param base the plain context underneath
 * @param string the string
 * @param nickname the author's Twitch name
 * @param channel the channel the message was posted in
 * @param reply the author and text of the replied-to message, or null
 */
@With
@Builder
record TwitchMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String string,
        @NonNull String nickname,
        @NonNull String channel,
        @Nullable Pair<String, String> reply
) implements TwitchMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new TwitchCacheKey(base().createCacheKey(), string, nickname, channel, reply);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param playerMessage the playerMessage
     * @param nickname the author's Twitch name
     * @param channel the channel the message was posted in
     * @param reply the author and text of the replied-to message, or null
     */
    public record TwitchCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            @NonNull String nickname,
            @NonNull String channel,
            @Nullable Pair<String, String> reply
    ) implements CacheKey {
    }

}