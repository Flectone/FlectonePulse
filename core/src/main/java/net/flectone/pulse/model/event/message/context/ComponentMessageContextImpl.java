package net.flectone.pulse.model.event.message.context;

import lombok.Builder;
import lombok.With;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

@With
@Builder
record ComponentMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull Component component
) implements ComponentMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new ComponentCacheKey(base().createCacheKey(), component);
    }

    public record ComponentCacheKey(
            @NonNull CacheKey base,
            @NonNull Component component
    ) implements CacheKey {
    }

}