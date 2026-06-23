package net.flectone.pulse.listener.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.player.PlayerPersistAndDisposeEvent;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.PlaytimeService;
import net.flectone.pulse.service.SocialService;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulsePlayerPersistAndDisposeListener implements PulseListener {

    private final FPlayerService fPlayerService;
    private final PlaytimeService playtimeService;
    private final SocialService socialService;

    @Pulse(priority = Event.Priority.LOW)
    public PlayerPersistAndDisposeEvent onPlayerPersistAndDispose(PlayerPersistAndDisposeEvent event) {
        FPlayer fPlayer = fPlayerService.clearAndSave(event.player());

        // update last session
        playtimeService.updateLastSession(fPlayer);

        // clear social cache
        socialService.invalidate(fPlayer.uuid());

        return event.withPlayer(fPlayer);
    }

}
