package net.flectone.pulse.module.command.ignore.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

@With
@Builder
record IgnoreMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull Ignore ignore,
        boolean ignored
) implements IgnoreMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new IgnoreCacheKey(base().createCacheKey(), ignore, ignored);
    }

    public record IgnoreCacheKey(
            @NonNull CacheKey base,
            @NonNull Ignore ignore,
            boolean ignored
    ) implements CacheKey {
    }

}