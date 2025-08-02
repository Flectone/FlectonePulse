package net.flectone.pulse.model.event.player;

import net.flectone.pulse.model.entity.FPlayer;

public class PlayerQuitEvent extends PlayerEvent {

    public PlayerQuitEvent(FPlayer fPlayer) {
        super(fPlayer);
    }

}
