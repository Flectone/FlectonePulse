package net.flectone.pulse.module.message.quit.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

@With
@Builder
record QuitMessageContextImpl(
        @NonNull MessageContext base,
        boolean fakeMessage,
        boolean vanished
) implements QuitMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new QuitCacheKey(base().createCacheKey(), fakeMessage, vanished);
    }

    public record QuitCacheKey(
            @NonNull CacheKey base,
            boolean fakeMessage,
            boolean vanished
    ) implements CacheKey {
    }

}