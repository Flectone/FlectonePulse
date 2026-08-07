package net.flectone.pulse.module.command.maintenance.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Moderation;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for a change to the server's maintenance state contexts.
 *
 * @param base the plain context underneath
 * @param moderation the moderation
 * @param turned whether maintenance was switched on, rather than off
 */
@With
@Builder
record MaintenanceMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull Moderation moderation,
        boolean turned
) implements MaintenanceMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new MaintenanceCacheKey(base().createCacheKey(), moderation, turned);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param moderation the moderation
     * @param turned whether maintenance was switched on, rather than off
     */
    public record MaintenanceCacheKey(
            @NonNull CacheKey base,
            @NonNull Moderation moderation,
            boolean turned
    ) implements CacheKey {
    }

}