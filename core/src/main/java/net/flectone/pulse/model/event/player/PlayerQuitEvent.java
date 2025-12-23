package net.flectone.pulse.model.event.player;

import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;

@With
public record PlayerQuitEvent(
        boolean cancelled,
        FPlayer player
) implements PlayerEvent {

    public PlayerQuitEvent(FPlayer player) {
        this(false, player);
    }

}