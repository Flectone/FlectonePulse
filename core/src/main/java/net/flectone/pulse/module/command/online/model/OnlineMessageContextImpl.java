package net.flectone.pulse.module.command.online.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

@With
@Builder
record OnlineMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String type
) implements OnlineMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new OnlineCacheKey(base().createCacheKey(), type);
    }

    public record OnlineCacheKey(
            @NonNull CacheKey base,
            @NonNull String type
    ) implements CacheKey {
    }

}