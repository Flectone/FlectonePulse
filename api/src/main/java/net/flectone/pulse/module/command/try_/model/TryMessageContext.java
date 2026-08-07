package net.flectone.pulse.module.command.try_.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;

/**
 * The outcome of a try roll.
 * Builds on {@link StringMessageContext}.
 * @author TheFaser
 */
public interface TryMessageContext extends StringMessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static TryMessageContextImpl.TryMessageContextImplBuilder builder() {
        return TryMessageContextImpl.builder();
    }

    /**
     * The rolled percentage, which decides success or failure.
     *
     * @return the rolled percentage, which decides success or failure
     */
    int percent();

    /**
     * Returns a copy carrying a different value.
     *
     * @param percent the rolled percentage, which decides success or failure
     * @return the copy
     */
    TryMessageContext withPercent(int percent);

}