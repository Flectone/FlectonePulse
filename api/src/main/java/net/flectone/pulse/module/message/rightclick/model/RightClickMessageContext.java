package net.flectone.pulse.module.message.rightclick.model;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * One player right-clicking another.
 * Builds on {@link MessageContext}.
 * @author TheFaser
 */
public interface RightClickMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static RightClickMessageContextImpl.RightClickMessageContextImplBuilder builder() {
        return RightClickMessageContextImpl.builder();
    }

    /**
     * The player who was clicked.
     *
     * @return the player who was clicked
     */
    @NonNull FPlayer target();

    /**
     * Returns a copy carrying a different value.
     *
     * @param target the player who was clicked
     * @return the copy
     */
    RightClickMessageContext withTarget(@NonNull FPlayer target);

}