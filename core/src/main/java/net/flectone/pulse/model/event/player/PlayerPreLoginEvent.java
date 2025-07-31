package net.flectone.pulse.model.event.player;

import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.model.FPlayer;
import net.kyori.adventure.text.Component;

@Setter
@Getter
public class PlayerPreLoginEvent extends PlayerEvent {

    private Component kickReason = Component.empty();
    private boolean allowed = true;

    public PlayerPreLoginEvent(FPlayer fPlayer) {
        super(fPlayer);
    }

}
