package net.flectone.pulse.module.command.coin.model;

import net.flectone.pulse.model.event.message.context.MessageContext;

/**
 * The outcome of a coin flip.
 * Builds on {@link MessageContext}.
 * @author TheFaser
 */
public interface CoinMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static CoinMessageContextImpl.CoinMessageContextImplBuilder builder() {
        return CoinMessageContextImpl.builder();
    }

    /**
     * The rolled percentage, which decides heads, tails or edge.
     *
     * @return the rolled percentage, which decides heads, tails or edge
     */
    int percent();

    /**
     * Returns a copy carrying a different value.
     *
     * @param percent the rolled percentage, which decides heads, tails or edge
     * @return the copy
     */
    CoinMessageContext withPercent(int percent);

}