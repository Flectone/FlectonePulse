package net.flectone.pulse.module.command.whitelist.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Moderation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Carrier for a change to the whitelist contexts.
 *
 * @param base the plain context underneath
 * @param moderation the whitelist entry, or null when the whitelist itself was toggled
 * @param turnedOn whether the whitelist was switched on, rather than off
 */
@With
@Builder
record WhitelistMessageContextImpl(
        @NonNull MessageContext base,
        @Nullable Moderation moderation,
        boolean turnedOn
) implements WhitelistMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new WhitelistCacheKey(base().createCacheKey(), moderation, turnedOn);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param moderation the whitelist entry, or null when the whitelist itself was toggled
     * @param turnedOn whether the whitelist was switched on, rather than off
     */
    public record WhitelistCacheKey(
            @NonNull CacheKey base,
            @Nullable Moderation moderation,
            boolean turnedOn
    ) implements CacheKey {
    }

}