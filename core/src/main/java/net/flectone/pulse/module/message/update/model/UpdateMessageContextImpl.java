package net.flectone.pulse.module.message.update.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

@With
@Builder
record UpdateMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String latestVersion,
        @NonNull String currentVersion
) implements UpdateMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new UpdateCacheKey(base().createCacheKey(), latestVersion, currentVersion);
    }

    public record UpdateCacheKey(
            @NonNull CacheKey base,
            @NonNull String latestVersion,
            @NonNull String currentVersion
    ) implements CacheKey {
    }

}