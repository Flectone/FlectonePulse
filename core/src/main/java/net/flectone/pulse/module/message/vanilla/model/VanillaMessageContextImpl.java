package net.flectone.pulse.module.message.vanilla.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@With
@Builder
record VanillaMessageContextImpl(
        @NonNull MessageContext base,
        @Nullable ParsedComponent parsedComponent,
        boolean fakeMessage,
        boolean vanished
) implements VanillaMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new VanillaCacheKey(base().createCacheKey(), parsedComponent, fakeMessage, vanished);
    }

    public record VanillaCacheKey(
            @NonNull CacheKey base,
            @Nullable ParsedComponent parsedComponent,
            boolean fakeMessage,
            boolean vanished
    ) implements CacheKey {
    }

}