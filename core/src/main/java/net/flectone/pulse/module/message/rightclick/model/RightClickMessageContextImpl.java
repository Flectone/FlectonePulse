package net.flectone.pulse.module.message.rightclick.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

@With
@Builder
record RightClickMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull FPlayer target
) implements RightClickMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new RightClickCacheKey(base().createCacheKey(), target);
    }

    public record RightClickCacheKey(
            @NonNull CacheKey base,
            @NonNull FPlayer target
    ) implements CacheKey {
    }

}