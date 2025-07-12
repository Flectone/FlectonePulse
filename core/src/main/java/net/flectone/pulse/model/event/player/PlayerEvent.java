package net.flectone.pulse.model.event.player;

import lombok.Getter;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.Event;

@Getter
public class PlayerEvent extends Event {

    private final FPlayer player;

    public PlayerEvent(Type type, FPlayer player) {
        super(type);

        this.player = player;
    }

}
