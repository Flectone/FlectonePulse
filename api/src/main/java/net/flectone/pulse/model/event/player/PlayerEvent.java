package net.flectone.pulse.model.event.player;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;

/**
 * An event about a single player.
 * @author TheFaser
 */
public interface PlayerEvent extends Event {

    /**
     * The player the event is about.
     *
     * @return the player
     */
    FPlayer player();

    /**
     * Returns a copy of this event for a different player.
     *
     * @param player the new player
     * @return the copy
     */
    PlayerEvent withPlayer(FPlayer player);

}