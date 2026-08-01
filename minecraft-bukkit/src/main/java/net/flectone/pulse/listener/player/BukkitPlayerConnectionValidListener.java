package net.flectone.pulse.listener.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.PlaytimeService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitPlayerConnectionValidListener implements Listener {

    private final FPlayerService fPlayerService;
    private final PlaytimeService playtimeService;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            UUID uuid = event.getUniqueId();
            fPlayerService.invalidateOnlineCache(uuid);
            playtimeService.invalidate(uuid);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerEvent(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            UUID uuid = event.getPlayer().getUniqueId();
            fPlayerService.invalidateOnlineCache(uuid);
            playtimeService.invalidate(uuid);
        }
    }

}
