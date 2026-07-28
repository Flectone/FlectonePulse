package net.flectone.pulse.module.command.geolocate.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

@With
@Builder
record GeolocateMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull IpResponse response
) implements GeolocateMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new GeolocateCacheKey(base().createCacheKey(), response);
    }

    public record GeolocateCacheKey(
            @NonNull CacheKey base,
            @NonNull IpResponse response
    ) implements CacheKey {
    }

}