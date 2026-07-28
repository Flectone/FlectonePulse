package net.flectone.pulse.module.command.dice.model;

import org.jspecify.annotations.NonNull;
import net.flectone.pulse.model.event.message.context.MessageContext;

import java.util.List;

public interface DiceMessageContext extends MessageContext {

    static DiceMessageContextImpl.DiceMessageContextImplBuilder builder() {
        return DiceMessageContextImpl.builder();
    }

    @NonNull List<Integer> cubes();

    DiceMessageContext withCubes(@NonNull List<Integer> cubes);

}