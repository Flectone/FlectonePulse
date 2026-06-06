package net.flectone.pulse.listener.player;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.processing.processor.PlayerPreLoginProcessor;
import net.flectone.pulse.processing.serializer.ComponentSerializer;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitPlayerLoginListener implements Listener {

    private final FileFacade fileFacade;
    private final MinecraftPacketProvider packetProvider;
    private final PlayerPreLoginProcessor playerPreLoginProcessor;
    private final ComponentSerializer componentSerializer;

    @EventHandler
    public void onAsyncPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        // in older versions (1.20.1 and older), there is no configuration stage, so we use Bukkit API
        if (!fileFacade.config().internal().usePacketLoginListener() || packetProvider.getServerVersion().isOlderThan(ServerVersion.V_1_20_2)) {
            UUID uuid = event.getUniqueId();
            String name = event.getName();

            playerPreLoginProcessor.processLogin(uuid, name, loginEvent -> {
                event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);

                Component reason = loginEvent.kickReason();
                event.setKickMessage(componentSerializer.toLegacy(reason));
            });
        }
    }

}
