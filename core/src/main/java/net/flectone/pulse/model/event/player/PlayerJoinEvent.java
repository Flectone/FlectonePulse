package net.flectone.pulse.model.event.player;

import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;

@With
public record PlayerJoinEvent(
        boolean cancelled,
        FPlayer player
) implements PlayerEvent {

    public PlayerJoinEvent(FPlayer player) {
        this(false, player);
    }

}