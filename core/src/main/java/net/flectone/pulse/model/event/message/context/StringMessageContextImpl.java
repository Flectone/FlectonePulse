package net.flectone.pulse.model.event.message.context;

import lombok.Builder;
import lombok.With;
import org.jspecify.annotations.NonNull;

@With
@Builder
record StringMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String string
) implements StringMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new StringCacheKey(base().createCacheKey(), string);
    }

    public record StringCacheKey(
            @NonNull CacheKey base,
            @NonNull String string
    ) implements CacheKey {
    }

}
