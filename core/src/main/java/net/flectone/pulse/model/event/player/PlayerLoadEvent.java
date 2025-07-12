package net.flectone.pulse.model.event.player;

import net.flectone.pulse.model.FPlayer;

public class PlayerLoadEvent extends PlayerEvent {

    public PlayerLoadEvent(FPlayer player) {
        super(Type.PLAYER_LOAD, player);
    }

}
