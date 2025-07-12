package net.flectone.pulse.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.event.player.PlayerPersistAndDisposeEvent;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.service.FPlayerService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

@Singleton
public class BukkitBaseListener implements Listener {

    private final FPlayerService fPlayerService;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public BukkitBaseListener(FPlayerService fPlayerService,
                              EventProcessRegistry eventProcessRegistry) {
        this.fPlayerService = fPlayerService;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        asyncProcessJoinEvent(event);
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        asyncProcessQuitEvent(event);
    }

    private void asyncProcessJoinEvent(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);
        eventProcessRegistry.processEvent(new PlayerLoadEvent(fPlayer));
        eventProcessRegistry.processEvent(new net.flectone.pulse.model.event.player.PlayerJoinEvent(fPlayer));
    }

    private void asyncProcessQuitEvent(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);

        eventProcessRegistry.processEvent(new net.flectone.pulse.model.event.player.PlayerQuitEvent(fPlayer));
        eventProcessRegistry.processEvent(new PlayerPersistAndDisposeEvent(fPlayer));
    }

}
