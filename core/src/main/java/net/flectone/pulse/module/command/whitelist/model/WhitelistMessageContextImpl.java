package net.flectone.pulse.module.command.whitelist.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Moderation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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

    public record WhitelistCacheKey(
            @NonNull CacheKey base,
            @Nullable Moderation moderation,
            boolean turnedOn
    ) implements CacheKey {
    }

}