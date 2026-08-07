package net.flectone.pulse.module.command.poll.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;
import net.flectone.pulse.module.command.poll.PollModule;
import org.jspecify.annotations.NonNull;

/**
 * A poll being created, voted on or closed.
 * Builds on {@link StringMessageContext}.
 * @author TheFaser
 */
public interface PollMessageContext extends StringMessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static PollMessageContextImpl.PollMessageContextImplBuilder builder() {
        return PollMessageContextImpl.builder();
    }

    /**
     * The poll.
     *
     * @return the poll
     */
    @NonNull Poll poll();

    /**
     * How the poll answered the request.
     *
     * @return how the poll answered the request
     */
    PollModule.@NonNull Status status();

    /**
     * What was done to the poll.
     *
     * @return what was done to the poll
     */
    PollModule.@NonNull Action action();

    /**
     * Returns a copy carrying a different value.
     *
     * @param poll the poll
     * @return the copy
     */
    PollMessageContext withPoll(@NonNull Poll poll);

    /**
     * Returns a copy carrying a different value.
     *
     * @param status how the poll answered the request
     * @return the copy
     */
    PollMessageContext withStatus(PollModule.@NonNull Status status);

    /**
     * Returns a copy carrying a different value.
     *
     * @param action what was done to the poll
     * @return the copy
     */
    PollMessageContext withAction(PollModule.@NonNull Action action);

}