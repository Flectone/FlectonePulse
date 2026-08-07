package net.flectone.pulse.model.event.player;

import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;
import net.kyori.adventure.text.Component;

/**
 * Fired before a player is let in, while the connection can still be refused.
 *
 * @param cancelled whether a listener vetoed the check
 * @param player the connecting player
 * @param kickReason the message shown when the player is refused
 * @param allowed whether the player may join
 * @author TheFaser
 */
@With
public record PlayerPreLoginEvent(
        boolean cancelled,
        FPlayer player,
        Component kickReason,
        boolean allowed
) implements PlayerEvent {

    /**
     * Creates an event that allows the player in with no kick reason.
     *
     * @param player the connecting player
     */
    public PlayerPreLoginEvent(FPlayer player) {
        this(false, player, Component.empty(), true);
    }

}