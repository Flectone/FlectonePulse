package net.flectone.pulse.module.message.afk.model;

import net.flectone.pulse.model.event.message.context.VanishMessageContext;

/**
 * A change to a player's afk state.
 * Builds on {@link VanishMessageContext}.
 * @author TheFaser
 */
public interface AFKMessageContext extends VanishMessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static AFKMessageContextImpl.AFKMessageContextImplBuilder builder() {
        return AFKMessageContextImpl.builder();
    }

    /**
     * Whether the player just went afk, rather than came back.
     *
     * @return whether the player just went afk, rather than came back
     */
    boolean newStatus();

    /**
     * Returns a copy carrying a different value.
     *
     * @param newStatus whether the player just went afk, rather than came back
     * @return the copy
     */
    AFKMessageContext withNewStatus(boolean newStatus);

}