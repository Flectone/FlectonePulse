package net.flectone.pulse.model.event.player;

import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;

/**
 * Fired when a player's data is written back and their cached state is dropped.
 *
 * @param cancelled whether a listener vetoed the work
 * @param player the player being persisted
 * @author TheFaser
 */
@With
public record PlayerPersistAndDisposeEvent(
        boolean cancelled,
        FPlayer player
) implements PlayerEvent {

    /**
     * Creates an event that has not been cancelled.
     *
     * @param player the player being persisted
     */
    public PlayerPersistAndDisposeEvent(FPlayer player) {
        this(false, player);
    }

}