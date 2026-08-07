package net.flectone.pulse.module.message.update.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for a notice that a newer plugin version exists contexts.
 *
 * @param base the plain context underneath
 * @param latestVersion the version available
 * @param currentVersion the version running
 */
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

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param latestVersion the version available
     * @param currentVersion the version running
     */
    public record UpdateCacheKey(
            @NonNull CacheKey base,
            @NonNull String latestVersion,
            @NonNull String currentVersion
    ) implements CacheKey {
    }

}