package net.flectone.pulse.module.command.ball.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;

/**
 * The reply of the magic-ball command.
 * Builds on {@link StringMessageContext}.
 * @author TheFaser
 */
public interface BallMessageContext extends StringMessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static BallMessageContextImpl.BallMessageContextImplBuilder builder() {
        return BallMessageContextImpl.builder();
    }

    /**
     * Index of the chosen answer in the localization list.
     *
     * @return index of the chosen answer in the localization list
     */
    int answer();

    /**
     * Returns a copy carrying a different value.
     *
     * @param answer index of the chosen answer in the localization list
     * @return the copy
     */
    BallMessageContext withAnswer(int answer);

}