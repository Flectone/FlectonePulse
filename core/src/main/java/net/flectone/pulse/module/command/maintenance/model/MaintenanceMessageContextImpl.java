package net.flectone.pulse.module.command.maintenance.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Moderation;
import org.jspecify.annotations.NonNull;

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

    public record MaintenanceCacheKey(
            @NonNull CacheKey base,
            @NonNull Moderation moderation,
            boolean turned
    ) implements CacheKey {
    }

}