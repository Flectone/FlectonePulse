package net.flectone.pulse.model.event.message.context;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.util.Moderation;
import org.jspecify.annotations.NonNull;

@With
@Builder
record ModerationMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull Moderation moderation
) implements ModerationMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new ModerationCacheKey(base().createCacheKey(), moderation);
    }

    public record ModerationCacheKey(
            @NonNull CacheKey base,
            @NonNull Moderation moderation
    ) implements CacheKey {
    }

}