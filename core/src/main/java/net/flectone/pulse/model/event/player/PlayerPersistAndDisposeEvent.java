package net.flectone.pulse.model.event.player;

import net.flectone.pulse.model.FPlayer;

public class PlayerPersistAndDisposeEvent extends PlayerEvent {

    public PlayerPersistAndDisposeEvent(FPlayer fPlayer) {
        super(Type.PLAYER_PERSIST_AND_DISPOSE, fPlayer);
    }

}
