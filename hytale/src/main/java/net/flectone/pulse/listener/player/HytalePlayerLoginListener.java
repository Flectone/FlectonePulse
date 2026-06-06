package net.flectone.pulse.listener.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.server.core.event.events.player.PlayerSetupConnectEvent;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.listener.HytaleListener;
import net.flectone.pulse.processing.processor.PlayerPreLoginProcessor;
import net.flectone.pulse.processing.serializer.HytaleComponentSerializer;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytalePlayerLoginListener implements HytaleListener {

    private final PlayerPreLoginProcessor playerPreLoginProcessor;
    private final HytaleComponentSerializer componentSerializer;

    public void onPlayerSetupConnectEvent(PlayerSetupConnectEvent event) {
        if (event.isCancelled()) return;

        UUID uuid = event.getUuid();
        String playerName = event.getUsername();
        playerPreLoginProcessor.processLogin(uuid, playerName, loginEvent -> {
            event.setReason(componentSerializer.toHytale(loginEvent.kickReason()));
            event.setCancelled(true);
        });
    }

}
