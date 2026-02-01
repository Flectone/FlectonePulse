package net.flectone.pulse.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.protocol.packets.interface_.UpdateLanguage;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerSetupConnectEvent;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketWatcher;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.event.player.PlayerPersistAndDisposeEvent;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.processing.processor.PlayerPreLoginProcessor;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleBaseListener implements HytaleListener {

    private final Set<UUID> disconnectPlayers = new CopyOnWriteArraySet<>();

    private final FPlayerService fPlayerService;
    private final EventDispatcher eventDispatcher;
    private final TaskScheduler taskScheduler;
    private final PlayerPreLoginProcessor playerPreLoginProcessor;

    public void onPlayerSetupConnectEvent(PlayerSetupConnectEvent event) {
        UUID uuid = event.getUuid();
        String playerName = event.getUsername();
        playerPreLoginProcessor.processLogin(uuid, playerName,
                loginEvent -> {},
                loginEvent -> {
            event.setReason(PlainTextComponentSerializer.plainText().serialize(loginEvent.kickReason()));
            event.setCancelled(true);
        });
    }

    // PlayerReadyEvent is called every time you move from portal to portal, this causes duplication
    // then use PlayerConnectEvent
    public void onPlayerConnectEvent(PlayerConnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        FPlayer fPlayer = fPlayerService.getFPlayer(playerRef.getUuid());

        fPlayerService.updateLocale(fPlayer, languageFormat(playerRef.getLanguage()));

        eventDispatcher.dispatch(new PlayerLoadEvent(fPlayer));
        eventDispatcher.dispatch(new PlayerJoinEvent(fPlayer));
    }

    // PlayerDisconnectEvent can be called multiple times, so we need to keep first disconnect and remove it later
    public void onPlayerDisconnectEvent(PlayerDisconnectEvent event) {
        UUID playerUUID = event.getPlayerRef().getUuid();
        if (disconnectPlayers.contains(playerUUID)) return;

        disconnectPlayers.add(playerUUID);

        taskScheduler.runAsync(() -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(playerUUID);

            eventDispatcher.dispatch(new PlayerQuitEvent(fPlayer));
            eventDispatcher.dispatch(new PlayerPersistAndDisposeEvent(fPlayer));
        });

        taskScheduler.runAsyncLater(() -> disconnectPlayers.remove(playerUUID));
    }

    public PlayerPacketWatcher createUpdateLanguageWatcher() {
        return (playerRef, packet) -> {
            if (packet instanceof UpdateLanguage updateLanguage) {
                String language = updateLanguage.language;
                taskScheduler.runAsync(() -> {
                    FPlayer fPlayer = fPlayerService.getFPlayer(playerRef.getUuid());
                    fPlayerService.updateLocale(fPlayer, languageFormat(language));
                });
            }
        };
    }

    private String languageFormat(String language) {
        if (StringUtils.isEmpty(language)) return "";

        return Strings.CS.replace(language.toLowerCase(Locale.ROOT), "-", "_");
    }

}
