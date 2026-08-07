package net.flectone.pulse.model.event.message.context;

import lombok.Builder;
import lombok.With;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for vanish-aware message contexts.
 *
 * @param base the plain context underneath
 * @param fakeMessage whether the message is a stand-in
 * @param vanished whether the sender is hidden
 */
@With
@Builder
record VanishMessageContextImpl(
        @NonNull MessageContext base,
        boolean fakeMessage,
        boolean vanished
) implements VanishMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new VanishCacheKey(base().createCacheKey(), fakeMessage, vanished);
    }

    /**
     * Cache key that mixes the vanish state into the base key.
     *
     * @param base the base key
     * @param fakeMessage whether the message is a stand-in
     * @param vanished whether the sender is hidden
     */
    public record VanishCacheKey(
            @NonNull CacheKey base,
            boolean fakeMessage,
            boolean vanished
    ) implements CacheKey {
    }

}
