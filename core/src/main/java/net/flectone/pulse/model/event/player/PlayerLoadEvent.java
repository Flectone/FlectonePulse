package net.flectone.pulse.model.event.player;

import lombok.Getter;
import net.flectone.pulse.model.entity.FPlayer;

@Getter
public class PlayerLoadEvent extends PlayerEvent {

    private final boolean reload;

    public PlayerLoadEvent(FPlayer player, boolean reload) {
        super(player);

        this.reload = reload;
    }

    public PlayerLoadEvent(FPlayer player) {
        this(player, false);
    }

}
