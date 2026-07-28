package net.flectone.pulse.module.message.afk.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

@With
@Builder
record AFKMessageContextImpl(
        @NonNull MessageContext base,
        boolean newStatus,
        boolean fakeMessage,
        boolean vanished
) implements AFKMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new AFKCacheKey(base().createCacheKey(), newStatus, fakeMessage, vanished);
    }

    public record AFKCacheKey(
            @NonNull CacheKey base,
            boolean newStatus,
            boolean fakeMessage,
            boolean vanished
    ) implements CacheKey {
    }

}