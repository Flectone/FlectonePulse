package net.flectone.pulse.listener.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.processing.serializer.NeoForgeComponentSerializer;
import net.flectone.pulse.processing.processor.PlayerPreLoginProcessor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NeoForgePlayerLoginListener {

    private final PlayerPreLoginProcessor playerPreLoginProcessor;
    private final NeoForgeComponentSerializer componentSerializer;

    public void onPreLogin(RegisterConfigurationTasksEvent event) {
        if (!(event.getListener() instanceof ServerConfigurationPacketListenerImpl packetListener)) {
            return;
        }

        GameProfile profile = packetListener.getOwner();
        if (profile == null) return;

        UUID uuid = profile.id();
        String name = profile.name();

        playerPreLoginProcessor.processLogin(uuid, name, loginEvent -> {
            try {
                packetListener.disconnect(componentSerializer.toNeoForge(loginEvent.kickReason()));
            } catch (Throwable _) {
                packetListener.disconnect(Component.empty());
            }
        });
    }

}
