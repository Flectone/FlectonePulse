package net.flectone.pulse.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.protocol.packets.connection.Disconnect;
import com.hypixel.hytale.protocol.packets.interface_.UpdateLanguage;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
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
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.processing.processor.PlayerPreLoginProcessor;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.Locale;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleBaseListener implements HytaleListener {

    private final FPlayerService fPlayerService;
    private final EventDispatcher eventDispatcher;
    private final TaskScheduler taskScheduler;
    private final PlayerPreLoginProcessor playerPreLoginProcessor;
    private final PlatformPlayerAdapter platformPlayerAdapter;

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
        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayerRef().getUuid());

        Object player = platformPlayerAdapter.convertToPlatformPlayer(fPlayer);
        if (player instanceof PlayerRef playerRef) {
            fPlayerService.updateLocale(fPlayer, languageFormat(playerRef.getLanguage()));
        }

        eventDispatcher.dispatch(new PlayerLoadEvent(fPlayer));
        eventDispatcher.dispatch(new PlayerJoinEvent(fPlayer));
    }

    public PlayerPacketWatcher createDisconnectWatcher() {
        return (playerRef, packet) -> {
            if (packet instanceof Disconnect) {
                onPlayerDisconnect(playerRef.getUuid());
            }
        };
    }

    public void onPlayerDisconnect(UUID uuid) {
        taskScheduler.runAsync(() -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(uuid);

            eventDispatcher.dispatch(new PlayerQuitEvent(fPlayer));
            eventDispatcher.dispatch(new PlayerPersistAndDisposeEvent(fPlayer));
        });
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
