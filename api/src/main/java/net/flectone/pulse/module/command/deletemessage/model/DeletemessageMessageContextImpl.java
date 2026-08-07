package net.flectone.pulse.module.command.deletemessage.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * Carrier for a chat message withdrawn by a moderator contexts.
 *
 * @param base the plain context underneath
 * @param deletedUUID id of the message being withdrawn
 */
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

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param deletedUUID id of the message being withdrawn
     */
    public record DeletemessageCacheKey(
            @NonNull CacheKey base,
            @NonNull UUID deletedUUID
    ) implements CacheKey {
    }

}