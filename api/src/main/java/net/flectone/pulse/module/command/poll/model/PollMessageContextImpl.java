package net.flectone.pulse.module.command.poll.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.poll.PollModule;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for a poll being created, voted on or closed contexts.
 *
 * @param base the plain context underneath
 * @param string the string
 * @param poll the poll
 * @param status how the poll answered the request
 * @param action what was done to the poll
 */
@With
@Builder
record PollMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String string,
        @NonNull Poll poll,
        PollModule.@NonNull Status status,
        PollModule.@NonNull Action action
) implements PollMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new PollCacheKey(base().createCacheKey(), string, poll, status, action);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param playerMessage the playerMessage
     * @param poll the poll
     * @param status how the poll answered the request
     * @param action what was done to the poll
     */
    public record PollCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            @NonNull Poll poll,
            PollModule.@NonNull Status status,
            PollModule.@NonNull Action action
    ) implements CacheKey {
    }

}