package net.flectone.pulse.listener;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.event.player.PlayerPersistAndDisposeEvent;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.file.FileFacade;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitBaseListener implements Listener {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final EventDispatcher eventDispatcher;
    private final MinecraftPacketProvider packetProvider;
    private final TaskScheduler taskScheduler;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinEvent(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        taskScheduler.runAsync(() -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(uuid);
            if (!fPlayer.isOnline()) return;

            if (packetProvider.getServerVersion().isOlderThan(ServerVersion.V_1_20_2)) {
                String locale = getPlayerLocale(player);
                fPlayerService.updateLocale(fPlayer, locale);
            }

            PlayerLoadEvent playerLoadEvent = eventDispatcher.dispatch(new PlayerLoadEvent(fPlayer));
            if (playerLoadEvent.cancelled()) return;

            PlayerJoinEvent playerJoinEvent = eventDispatcher.dispatch(new PlayerJoinEvent(playerLoadEvent.player()));
            if (playerJoinEvent.cancelled()) {
                // nothing
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuitEvent(org.bukkit.event.player.PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

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

    private String getPlayerLocale(Player player) {
        try {
            return player.getLocale();
        } catch (NoSuchMethodError _) {
            return fileFacade.config().language().type();
        }
    }
}
