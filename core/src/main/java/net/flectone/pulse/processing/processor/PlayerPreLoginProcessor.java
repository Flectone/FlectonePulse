package net.flectone.pulse.processing.processor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerPreLoginEvent;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.PlaytimeService;

import java.util.UUID;
import java.util.function.Consumer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerPreLoginProcessor {

    private final FPlayerService fPlayerService;
    private final EventDispatcher eventDispatcher;
    private final PlaytimeService playtimeService;

    public void processLogin(UUID uuid, String name, Consumer<PlayerPreLoginEvent> kickConsumer) {
        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);

        // if player is unknown, then he is not in database and has never been on the server before this moment
        // try to search by name
        if (fPlayer.isUnknown()) {
            fPlayer = fPlayerService.getFPlayer(name);
        }

        PlayerPreLoginEvent event = eventDispatcher.dispatch(new PlayerPreLoginEvent(fPlayer));
        if (!event.allowed()) {
            fPlayerService.invalidateOnlineCache(fPlayer.uuid());
            playtimeService.invalidate(fPlayer.uuid());
            kickConsumer.accept(event);
        }
    }
}
