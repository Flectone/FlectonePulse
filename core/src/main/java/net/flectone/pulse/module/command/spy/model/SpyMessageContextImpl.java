package net.flectone.pulse.module.command.spy.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

@With
@Builder
record SpyMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String string,
        boolean turned,
        @NonNull String action
) implements SpyMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new SpyCacheKey(base().createCacheKey(), string, turned, action);
    }

    public record SpyCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            boolean turned,
            @NonNull String action
    ) implements CacheKey {
    }

}