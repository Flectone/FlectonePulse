package net.flectone.pulse.module.command.deletemessage.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

@With
@Builder
record DeletemessageMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull UUID deletedUUID
) implements DeletemessageMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new DeletemessageCacheKey(base().createCacheKey(), deletedUUID);
    }

    public record DeletemessageCacheKey(
            @NonNull CacheKey base,
            @NonNull UUID deletedUUID
    ) implements CacheKey {
    }

}