package net.flectone.pulse.module.command.nickname.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for a change to a player's nickname contexts.
 *
 * @param base the plain context underneath
 * @param nickname the new nickname
 */
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

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param nickname the new nickname
     */
    public record NicknameCacheKey(
            @NonNull CacheKey base,
            @NonNull String nickname
    ) implements CacheKey {
    }

}