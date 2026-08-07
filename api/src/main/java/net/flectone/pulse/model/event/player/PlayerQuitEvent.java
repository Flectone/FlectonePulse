package net.flectone.pulse.model.event.player;

import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;

/**
 * Fired when a player leaves the server.
 *
 * @param cancelled whether a listener suppressed the quit handling
 * @param player the leaving player
 * @author TheFaser
 */
@With
public record PlayerQuitEvent(
        boolean cancelled,
        FPlayer player
) implements PlayerEvent {

    /**
     * Creates an event that has not been cancelled.
     *
     * @param player the leaving player
     */
    public PlayerQuitEvent(FPlayer player) {
        this(false, player);
    }

}