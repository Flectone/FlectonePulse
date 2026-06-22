package net.flectone.pulse.listener.player;

import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.processing.processor.PlayerPreLoginProcessor;
import net.flectone.pulse.processing.serializer.ComponentSerializer;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NeoForgePlayerLoginListener {

    private final PlayerPreLoginProcessor playerPreLoginProcessor;
    private final ComponentSerializer componentSerializer;

    public void onPreLogin(RegisterConfigurationTasksEvent event) {
        if (!(event.getListener() instanceof ServerConfigurationPacketListenerImpl packetListener)) {
            return;
        }

        GameProfile profile = packetListener.getOwner();
        if (profile == null) return;

        UUID uuid = profile.id();
        String name = profile.name();

        playerPreLoginProcessor.processLogin(uuid, name, loginEvent -> {
            Component reason = loginEvent.kickReason();
            JsonElement jsonElement = componentSerializer.toJsonTree(reason);

            try {
                net.minecraft.network.chat.Component minecraftComponent = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, jsonElement).getOrThrow();
                packetListener.disconnect(minecraftComponent);
            } catch (IllegalStateException _) {
                packetListener.disconnect(net.minecraft.network.chat.Component.empty());
            }
        });
    }
}
