package net.flectone.pulse.model.event.player;

import lombok.Getter;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;

@Getter
public abstract class PlayerEvent extends Event {

    private final FPlayer player;

    protected PlayerEvent(FPlayer player) {
        this.player = player;
    }

}
