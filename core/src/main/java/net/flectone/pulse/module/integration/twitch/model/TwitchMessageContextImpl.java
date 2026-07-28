package net.flectone.pulse.module.integration.twitch.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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

    public record TwitchCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            @NonNull String nickname,
            @NonNull String channel,
            @Nullable Pair<String, String> reply
    ) implements CacheKey {
    }

}