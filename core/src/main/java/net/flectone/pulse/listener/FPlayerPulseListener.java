package net.flectone.pulse.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerPersistAndDisposeEvent;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.service.FPlayerService;

@Singleton
public class FPlayerPulseListener implements PulseListener {
    
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Inject
    public FPlayerPulseListener(FPlayerService fPlayerService,
                                PlatformPlayerAdapter platformPlayerAdapter) {
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
    }

    @Pulse(priority = Event.Priority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.getPlayer();

        // set correct ip
        fPlayer.setIp(platformPlayerAdapter.getIp(fPlayer));

        fPlayerService.saveFPlayerData(fPlayer);
    }

    @Pulse
    public void onPlayerPersistAndDispose(PlayerPersistAndDisposeEvent event) {
        FPlayer fPlayer = event.getPlayer();
        fPlayerService.clearAndSave(fPlayer);
    }
}
