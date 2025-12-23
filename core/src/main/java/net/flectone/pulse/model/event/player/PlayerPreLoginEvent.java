package net.flectone.pulse.model.event.player;

import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;
import net.kyori.adventure.text.Component;

@With
public record PlayerPreLoginEvent(
        boolean cancelled,
        FPlayer player,
        Component kickReason,
        boolean allowed
) implements PlayerEvent {

    public PlayerPreLoginEvent(FPlayer player) {
        this(false, player, Component.empty(), true);
    }

}