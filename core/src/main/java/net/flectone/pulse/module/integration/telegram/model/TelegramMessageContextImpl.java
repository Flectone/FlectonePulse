package net.flectone.pulse.module.integration.telegram.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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