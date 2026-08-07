package net.flectone.pulse.module.command.online.model;

import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * The answer of the online command.
 * Builds on {@link MessageContext}.
 * @author TheFaser
 */
public interface OnlineMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static OnlineMessageContextImpl.OnlineMessageContextImplBuilder builder() {
        return OnlineMessageContextImpl.builder();
    }

    /**
     * Which player count was asked for.
     *
     * @return which player count was asked for
     */
    @NonNull String type();

    /**
     * Returns a copy carrying a different value.
     *
     * @param type which player count was asked for
     * @return the copy
     */
    OnlineMessageContext withType(@NonNull String type);

}