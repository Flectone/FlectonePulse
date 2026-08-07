package net.flectone.pulse.module.message.vanilla.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Carrier for a vanilla server message the plugin re-renders contexts.
 *
 * @param base the plain context underneath
 * @param parsedComponent the message broken into its translation key and arguments, or null if it could not be parsed
 * @param fakeMessage the fakeMessage
 * @param vanished the vanished
 */
@With
@Builder
record VanillaMessageContextImpl(
        @NonNull MessageContext base,
        @Nullable ParsedComponent parsedComponent,
        boolean fakeMessage,
        boolean vanished
) implements VanillaMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new VanillaCacheKey(base().createCacheKey(), parsedComponent, fakeMessage, vanished);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param parsedComponent the message broken into its translation key and arguments, or null if it could not be parsed
     * @param fakeMessage the fakeMessage
     * @param vanished the vanished
     */
    public record VanillaCacheKey(
            @NonNull CacheKey base,
            @Nullable ParsedComponent parsedComponent,
            boolean fakeMessage,
            boolean vanished
    ) implements CacheKey {
    }

}