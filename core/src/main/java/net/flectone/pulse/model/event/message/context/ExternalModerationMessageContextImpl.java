package net.flectone.pulse.model.event.message.context;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.util.ExternalModeration;
import org.jspecify.annotations.NonNull;

@With
@Builder
record ExternalModerationMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull ExternalModeration externalModeration
) implements ExternalModerationMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new ExternalModerationCacheKey(base().createCacheKey(), externalModeration);
    }

    public record ExternalModerationCacheKey(
            @NonNull CacheKey base,
            @NonNull ExternalModeration externalModeration
    ) implements CacheKey {
    }

}