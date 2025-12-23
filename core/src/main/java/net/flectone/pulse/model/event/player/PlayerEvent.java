package net.flectone.pulse.model.event.player;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;

public interface PlayerEvent extends Event {

    FPlayer player();

    PlayerEvent withPlayer(FPlayer player);

}