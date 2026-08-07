package net.flectone.pulse.module.command.dice.model;

import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * The outcome of a dice roll.
 * Builds on {@link MessageContext}.
 * @author TheFaser
 */
public interface DiceMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static DiceMessageContextImpl.DiceMessageContextImplBuilder builder() {
        return DiceMessageContextImpl.builder();
    }

    /**
     * The value rolled on each die.
     *
     * @return the value rolled on each die
     */
    @NonNull List<Integer> cubes();

    /**
     * Returns a copy carrying a different value.
     *
     * @param cubes the value rolled on each die
     * @return the copy
     */
    DiceMessageContext withCubes(@NonNull List<Integer> cubes);

}