package net.flectone.pulse.model.event.player;

import net.flectone.pulse.model.entity.FPlayer;

public class PlayerLoadEvent extends PlayerEvent {

    public PlayerLoadEvent(FPlayer player) {
        super(player);
    }

}
