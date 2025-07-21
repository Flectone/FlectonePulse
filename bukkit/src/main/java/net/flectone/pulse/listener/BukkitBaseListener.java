package net.flectone.pulse.listener;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.event.player.PlayerPersistAndDisposeEvent;
import net.flectone.pulse.processor.PlayerPreLoginProcessor;
import net.flectone.pulse.provider.PacketProvider;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

@Singleton
public class BukkitBaseListener implements Listener {

    private final Config config;
    private final FPlayerService fPlayerService;
    private final EventProcessRegistry eventProcessRegistry;
    private final PacketProvider packetProvider;
    private final PlayerPreLoginProcessor playerPreLoginProcessor;

    @Inject
    public BukkitBaseListener(FileResolver fileResolver,
                              FPlayerService fPlayerService,
                              EventProcessRegistry eventProcessRegistry,
                              PacketProvider packetProvider,
                              PlayerPreLoginProcessor playerPreLoginProcessor) {
        this.config = fileResolver.getConfig();
        this.fPlayerService = fPlayerService;
        this.eventProcessRegistry = eventProcessRegistry;
        this.packetProvider = packetProvider;
        this.playerPreLoginProcessor = playerPreLoginProcessor;
    }

    @EventHandler
    public void onAsyncPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        // in older versions (1.20.1 and older), there is no configuration stage
        // so we use Bukkit API
        if (packetProvider.getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_20_1)) {
            UUID uuid = event.getUniqueId();
            String name = event.getName();

            playerPreLoginProcessor.processLogin(uuid, name, loginEvent -> {}, loginEvent -> {
                event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);

                Component reason = loginEvent.getKickReason();
                event.setKickMessage(LegacyComponentSerializer.legacySection().serialize(reason));
            });
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        asyncProcessJoinEvent(event);
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        asyncProcessQuitEvent(event);
    }

    @Async
    public void asyncProcessJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);
        if (packetProvider.getServerVersion().isOlderThan(ServerVersion.V_1_20_2)) {
            String locale = getPlayerLocale(player);
            fPlayerService.updateLocale(fPlayer, locale);
        }

        eventProcessRegistry.processEvent(new PlayerLoadEvent(fPlayer));
        eventProcessRegistry.processEvent(new net.flectone.pulse.model.event.player.PlayerJoinEvent(fPlayer));
    }

    @Async
    public void asyncProcessQuitEvent(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);

        eventProcessRegistry.processEvent(new net.flectone.pulse.model.event.player.PlayerQuitEvent(fPlayer));
        eventProcessRegistry.processEvent(new PlayerPersistAndDisposeEvent(fPlayer));
    }

    private String getPlayerLocale(Player player) {
        try {
            return player.getLocale();
        } catch (NoSuchMethodError e) {
            return config.getLanguage();
        }
    }
}
