package net.flectone.pulse.module.command.poll.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;
import net.flectone.pulse.module.command.poll.PollModule;
import org.jspecify.annotations.NonNull;

public interface PollMessageContext extends StringMessageContext {

    static PollMessageContextImpl.PollMessageContextImplBuilder builder() {
        return PollMessageContextImpl.builder();
    }

    @NonNull Poll poll();

    PollModule.@NonNull Status status();

    PollModule.@NonNull Action action();

    PollMessageContext withPoll(@NonNull Poll poll);

    PollMessageContext withStatus(PollModule.@NonNull Status status);

    PollMessageContext withAction(PollModule.@NonNull Action action);

}