package net.flectone.pulse.model.event.player;

import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;

/**
 * Fired once a player's stored data has been read and they are ready to be messaged.
 *
 * @param cancelled whether a listener suppressed the load handling
 * @param player the loaded player
 * @param reload whether this happened during a plugin reload rather than a fresh join
 * @author TheFaser
 */
@With
public record PlayerLoadEvent(
        boolean cancelled,
        FPlayer player,
        boolean reload
) implements PlayerEvent {

    /**
     * Creates an event that has not been cancelled.
     *
     * @param player the loaded player
     * @param reload whether this happened during a reload
     */
    public PlayerLoadEvent(FPlayer player, boolean reload) {
        this(false, player, reload);
    }

    /**
     * Creates an event for a fresh join rather than a reload.
     *
     * @param player the loaded player
     */
    public PlayerLoadEvent(FPlayer player) {
        this(player, false);
    }

}