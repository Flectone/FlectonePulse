package net.flectone.pulse.module.command.try_.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

@With
@Builder
record TryMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String string,
        int percent
) implements TryMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new TryCacheKey(base().createCacheKey(), string, percent);
    }

    public record TryCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            int percent
    ) implements CacheKey {
    }

}