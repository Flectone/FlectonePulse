package net.flectone.pulse.model.event.player;

import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;

/**
 * Fired when a player joins the server.
 *
 * @param cancelled whether a listener suppressed the join handling
 * @param player the joining player
 * @author TheFaser
 */
@With
public record PlayerJoinEvent(
        boolean cancelled,
        FPlayer player
) implements PlayerEvent {

    /**
     * Creates an event that has not been cancelled.
     *
     * @param player the joining player
     */
    public PlayerJoinEvent(FPlayer player) {
        this(false, player);
    }

}