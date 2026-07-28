package net.flectone.pulse.module.command.rockpaperscissors.model;

import org.jspecify.annotations.NonNull;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.rockpaperscissors.RockpaperscissorsModule;

public interface RockPaperScissorsMessageContext extends MessageContext {

    static RockPaperScissorsMessageContextImpl.RockPaperScissorsMessageContextImplBuilder builder() {
        return RockPaperScissorsMessageContextImpl.builder();
    }

    @NonNull RockPaperScissors rockPaperScissors();

    RockpaperscissorsModule.@NonNull GamePhase gamePhase();

    RockPaperScissorsMessageContext withRockPaperScissors(@NonNull RockPaperScissors rockPaperScissors);

    RockPaperScissorsMessageContext withGamePhase(RockpaperscissorsModule.@NonNull GamePhase gamePhase);

}