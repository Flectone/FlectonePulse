package net.flectone.pulse.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.player.PlayerPersistAndDisposeEvent;
import net.flectone.pulse.service.FPlayerService;

@Singleton
public class FPlayerPulseListener implements PulseListener {
    
    private final FPlayerService fPlayerService;

    @Inject
    public FPlayerPulseListener(FPlayerService fPlayerService) {
        this.fPlayerService = fPlayerService;
    }

    @Pulse
    public void onPlayerPersistAndDispose(PlayerPersistAndDisposeEvent event) {
        FPlayer fPlayer = event.getPlayer();
        fPlayerService.clearAndSave(fPlayer);
    }
}
