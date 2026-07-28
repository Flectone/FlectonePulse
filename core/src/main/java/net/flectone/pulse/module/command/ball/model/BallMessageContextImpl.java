package net.flectone.pulse.module.command.ball.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

@With
@Builder
record BallMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String string,
        int answer
) implements BallMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new BallCacheKey(base().createCacheKey(), string, answer);
    }

    public record BallCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            int answer
    ) implements CacheKey {
    }

}