package net.flectone.pulse.module.command.poll.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.poll.PollModule;
import org.jspecify.annotations.NonNull;

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

    public record PollCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            @NonNull Poll poll,
            PollModule.@NonNull Status status,
            PollModule.@NonNull Action action
    ) implements CacheKey {
    }

}