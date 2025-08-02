package net.flectone.pulse.model.event.player;

import net.flectone.pulse.model.entity.FPlayer;

public class PlayerPersistAndDisposeEvent extends PlayerEvent {

    public PlayerPersistAndDisposeEvent(FPlayer fPlayer) {
        super(fPlayer);
    }

}
