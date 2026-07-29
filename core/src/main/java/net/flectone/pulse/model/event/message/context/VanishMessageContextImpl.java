package net.flectone.pulse.model.event.message.context;

import lombok.Builder;
import lombok.With;
import org.jspecify.annotations.NonNull;

@With
@Builder
record VanishMessageContextImpl(
        @NonNull MessageContext base,
        boolean fakeMessage,
        boolean vanished
) implements VanishMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new VanishCacheKey(base().createCacheKey(), fakeMessage, vanished);
    }

    public record VanishCacheKey(
            @NonNull CacheKey base,
            boolean fakeMessage,
            boolean vanished
    ) implements CacheKey {
    }

}
