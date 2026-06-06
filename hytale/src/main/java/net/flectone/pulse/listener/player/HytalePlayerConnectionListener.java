package net.flectone.pulse.listener.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.listener.HytaleListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.event.player.PlayerPersistAndDisposeEvent;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.service.FPlayerService;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytalePlayerConnectionListener implements HytaleListener {

    private final Set<UUID> disconnectPlayers = new CopyOnWriteArraySet<>();

    private final FPlayerService fPlayerService;
    private final EventDispatcher eventDispatcher;
    private final TaskScheduler taskScheduler;

    // PlayerReadyEvent is called every time you move from portal to portal, this causes duplication
    // then use PlayerConnectEvent
    public void onPlayerConnectEvent(PlayerConnectEvent event) {
        UUID uuid = event.getPlayerRef().getUuid();

        taskScheduler.runAsync(() -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(uuid);

            PlayerLoadEvent playerLoadEvent = eventDispatcher.dispatch(new PlayerLoadEvent(fPlayer));
            if (playerLoadEvent.cancelled()) return;

            PlayerJoinEvent playerJoinEvent = eventDispatcher.dispatch(new PlayerJoinEvent(playerLoadEvent.player()));
            if (playerJoinEvent.cancelled()) {
                // nothing
            }
        });
    }

    // PlayerDisconnectEvent can be called multiple times, so we need to keep first disconnect and remove it later
    public void onPlayerDisconnectEvent(PlayerDisconnectEvent event) {
        UUID playerUUID = event.getPlayerRef().getUuid();

        if (!disconnectPlayers.add(playerUUID)) {
            return;
        }

        taskScheduler.runAsync(() -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(playerUUID);
            if (!fPlayer.isOnline()) return;

            PlayerQuitEvent playerQuitEvent = eventDispatcher.dispatch(new PlayerQuitEvent(fPlayer));
            if (playerQuitEvent.cancelled()) return;

            PlayerPersistAndDisposeEvent playerPersistAndDisposeEvent = eventDispatcher.dispatch(new PlayerPersistAndDisposeEvent(playerQuitEvent.player()));
            if (playerPersistAndDisposeEvent.cancelled()) {
                // nothing
            }
        });

        taskScheduler.runAsyncLater(() -> disconnectPlayers.remove(playerUUID));
    }

}
