package net.flectone.pulse.module.command.tell.model;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.StringMessageContext;
import org.jspecify.annotations.NonNull;

/**
 * A private-message context that carries the target player.
 * Builds on {@link StringMessageContext}.
 *
 * @author TheFaser
 */
public interface TellMessageContext extends StringMessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static TellMessageContextImpl.TellMessageContextImplBuilder builder() {
        return TellMessageContextImpl.builder();
    }

    /**
     * The player receiving the message.
     *
     * @return the target player
     */
    @NonNull FPlayer target();

    /**
     * Returns a copy carrying a different target.
     *
     * @param target the target player
     * @return the copy
     */
    TellMessageContext withTarget(@NonNull FPlayer target);

}