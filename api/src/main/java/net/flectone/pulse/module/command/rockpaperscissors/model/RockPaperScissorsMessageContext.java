package net.flectone.pulse.module.command.rockpaperscissors.model;

import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.rockpaperscissors.RockpaperscissorsModule;
import org.jspecify.annotations.NonNull;

/**
 * A round of rock-paper-scissors.
 * Builds on {@link MessageContext}.
 * @author TheFaser
 */
public interface RockPaperScissorsMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static RockPaperScissorsMessageContextImpl.RockPaperScissorsMessageContextImplBuilder builder() {
        return RockPaperScissorsMessageContextImpl.builder();
    }

    /**
     * The game.
     *
     * @return the game
     */
    @NonNull RockPaperScissors rockPaperScissors();

    /**
     * How far the round has got.
     *
     * @return how far the round has got
     */
    RockpaperscissorsModule.@NonNull GamePhase gamePhase();

    /**
     * Returns a copy carrying a different value.
     *
     * @param rockPaperScissors the game
     * @return the copy
     */
    RockPaperScissorsMessageContext withRockPaperScissors(@NonNull RockPaperScissors rockPaperScissors);

    /**
     * Returns a copy carrying a different value.
     *
     * @param gamePhase how far the round has got
     * @return the copy
     */
    RockPaperScissorsMessageContext withGamePhase(RockpaperscissorsModule.@NonNull GamePhase gamePhase);

}