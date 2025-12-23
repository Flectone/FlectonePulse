package net.flectone.pulse.model.event.player;

import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;

@With
public record PlayerPersistAndDisposeEvent(
        boolean cancelled,
        FPlayer player
) implements PlayerEvent {

    public PlayerPersistAndDisposeEvent(FPlayer player) {
        this(false, player);
    }

}