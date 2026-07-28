package net.flectone.pulse.module.command.coin.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

@With
@Builder
record CoinMessageContextImpl(
        @NonNull MessageContext base,
        int percent
) implements CoinMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new CoinCacheKey(base().createCacheKey(), percent);
    }

    public record CoinCacheKey(
            @NonNull CacheKey base,
            int percent
    ) implements CacheKey {
    }

}