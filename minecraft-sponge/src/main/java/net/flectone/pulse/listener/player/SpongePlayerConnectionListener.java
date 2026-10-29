package net.flectone.pulse.listener.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.dispatcher.EventDispatcher;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.event.player.PlayerPersistAndDisposeEvent;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpongePlayerConnectionListener {

    private final FPlayerService fPlayerService;
    private final EventDispatcher eventDispatcher;
    private final TaskScheduler taskScheduler;
    private final ModuleController moduleController;

    @Listener(order = Order.LAST)
    public void onJoinEvent(ServerSideConnectionEvent.Join event) {
        if (moduleController.isEnable(ModuleName.MESSAGE_JOIN)) {
            event.setMessageCancelled(true);
        }

        UUID uuid = event.player().uniqueId();

        taskScheduler.runAsyncLater(() -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(uuid);

            PlayerLoadEvent playerLoadEvent = eventDispatcher.dispatch(new PlayerLoadEvent(fPlayer));
            if (playerLoadEvent.cancelled()) return;

            PlayerJoinEvent playerJoinEvent = eventDispatcher.dispatch(new PlayerJoinEvent(playerLoadEvent.player()));
            if (playerJoinEvent.cancelled()) {
                // nothing
            }
        }, 1L);
    }

    @Listener(order = Order.LAST)
    public void onLeaveEvent(ServerSideConnectionEvent.Leave event) {
        if (moduleController.isEnable(ModuleName.MESSAGE_QUIT)) {
            event.setMessageCancelled(true);
        }

        UUID uuid = event.player().uniqueId();

        taskScheduler.runAsync(() -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(uuid);
            if (!fPlayer.isOnline()) return;

            PlayerQuitEvent playerQuitEvent = eventDispatcher.dispatch(new PlayerQuitEvent(fPlayer));
            if (playerQuitEvent.cancelled()) return;

            PlayerPersistAndDisposeEvent playerPersistAndDisposeEvent = eventDispatcher.dispatch(new PlayerPersistAndDisposeEvent(playerQuitEvent.player()));
            if (playerPersistAndDisposeEvent.cancelled()) {
                // nothing
            }
        });
    }
}
