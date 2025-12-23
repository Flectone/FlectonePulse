package net.flectone.pulse.model.event.player;

import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;

@With
public record PlayerLoadEvent(
        boolean cancelled,
        FPlayer player,
        boolean reload
) implements PlayerEvent {

    public PlayerLoadEvent(FPlayer player, boolean reload) {
        this(false, player, reload);
    }

    public PlayerLoadEvent(FPlayer player) {
        this(player, false);
    }

}