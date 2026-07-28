package net.flectone.pulse.module.message.join.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

@With
@Builder
record JoinMessageContextImpl(
        @NonNull MessageContext base,
        boolean playedBefore,
        boolean fakeMessage,
        boolean vanished
) implements JoinMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new JoinCacheKey(base().createCacheKey(), playedBefore, fakeMessage, vanished);
    }

    public record JoinCacheKey(
            @NonNull CacheKey base,
            boolean playedBefore,
            boolean fakeMessage,
            boolean vanished
    ) implements CacheKey {
    }

}