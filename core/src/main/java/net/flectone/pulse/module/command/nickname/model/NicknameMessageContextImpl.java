package net.flectone.pulse.module.command.nickname.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

@With
@Builder
record NicknameMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String nickname
) implements NicknameMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new NicknameCacheKey(base().createCacheKey(), nickname);
    }

    public record NicknameCacheKey(
            @NonNull CacheKey base,
            @NonNull String nickname
    ) implements CacheKey {
    }

}