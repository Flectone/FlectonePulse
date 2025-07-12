package net.flectone.pulse.model.event.player;

import net.flectone.pulse.model.FPlayer;

public class PlayerQuitEvent extends PlayerEvent {

    public PlayerQuitEvent(FPlayer fPlayer) {
        super(Type.PLAYER_QUIT, fPlayer);
    }

}
