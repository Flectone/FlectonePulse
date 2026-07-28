package net.flectone.pulse.module.integration.discord.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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