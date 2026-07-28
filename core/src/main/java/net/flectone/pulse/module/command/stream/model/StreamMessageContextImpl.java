package net.flectone.pulse.module.command.stream.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@With
@Builder
record StreamMessageContextImpl(
        @NonNull MessageContext base,
        boolean turned,
        @Nullable String urls
) implements StreamMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new StreamCacheKey(base().createCacheKey(), turned, urls);
    }

    public record StreamCacheKey(
            @NonNull CacheKey base,
            boolean turned,
            @Nullable String urls
    ) implements CacheKey {
    }

}