package net.flectone.pulse.module.message.objective.belowname.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.module.message.objective.belowname.BelownameModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BelownamePulseListener implements PulseListener {

    private final BelownameModule belownameModule;

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        FPlayer fPlayer = event.getPlayer();
        belownameModule.create(fPlayer);
    }

    @Pulse
    public void onPlayerQuit(PlayerQuitEvent event) {
        FPlayer fPlayer = event.getPlayer();
        belownameModule.remove(fPlayer);
    }


}
