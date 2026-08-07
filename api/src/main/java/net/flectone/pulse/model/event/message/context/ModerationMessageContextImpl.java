package net.flectone.pulse.model.event.message.context;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.value.Moderation;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for moderation-flavored message contexts.
 *
 * @param base the plain context underneath
 * @param moderation the extra value
 */
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

    /**
     * Cache key that mixes the extra value into the base key.
     *
     * @param base the base key
     * @param moderation the extra value
     */
    public record ModerationCacheKey(
            @NonNull CacheKey base,
            @NonNull Moderation moderation
    ) implements CacheKey {
    }

}