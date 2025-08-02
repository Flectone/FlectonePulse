package net.flectone.pulse.model.event.player;

import net.flectone.pulse.model.entity.FPlayer;

public class PlayerJoinEvent extends PlayerEvent {

    public PlayerJoinEvent(FPlayer player) {
        super(player);
    }

}
