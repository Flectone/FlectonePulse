package net.flectone.pulse.module.message.join.model;

import net.flectone.pulse.model.event.message.context.VanishMessageContext;

/**
 * A player joining the server.
 * Builds on {@link VanishMessageContext}.
 * @author TheFaser
 */
public interface JoinMessageContext extends VanishMessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static JoinMessageContextImpl.JoinMessageContextImplBuilder builder() {
        return JoinMessageContextImpl.builder();
    }

    /**
     * Whether the player has been here before, which picks the first-join wording.
     *
     * @return whether the player has been here before, which picks the first-join wording
     */
    boolean playedBefore();

    /**
     * Returns a copy carrying a different value.
     *
     * @param playedBefore whether the player has been here before, which picks the first-join wording
     * @return the copy
     */
    JoinMessageContext withPlayedBefore(boolean playedBefore);

}