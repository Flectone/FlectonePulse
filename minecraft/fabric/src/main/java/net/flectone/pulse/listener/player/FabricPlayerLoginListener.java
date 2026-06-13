package net.flectone.pulse.listener.player;

import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.flectone.pulse.mixin.ServerLoginPacketListenerImplAccessor;
import net.flectone.pulse.processing.processor.PlayerPreLoginProcessor;
import net.flectone.pulse.processing.serializer.ComponentSerializer;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricPlayerLoginListener {

    private final FileFacade fileFacade;
    private final PlayerPreLoginProcessor playerPreLoginProcessor;
    private final ComponentSerializer componentSerializer;

    public void onPreLogin(ServerLoginPacketListenerImpl netHandler, MinecraftServer server, PacketSender packetSender, ServerLoginNetworking.LoginSynchronizer synchronizer) {
        if (fileFacade.config().internal().usePacketLoginListener()) return;
        if (!netHandler.isAcceptingMessages()) return;

        GameProfile profile = ((ServerLoginPacketListenerImplAccessor) netHandler).getAuthenticatedProfile();
        UUID uuid = profile.id();
        String name = profile.name();

        playerPreLoginProcessor.processLogin(uuid, name, loginEvent -> {
            Component reason = loginEvent.kickReason();
            JsonElement jsonElement = componentSerializer.toJsonTree(reason);

            try {
                net.minecraft.network.chat.Component minecraftComponent = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, jsonElement).getOrThrow();
                netHandler.disconnect(minecraftComponent);
            } catch (IllegalStateException _) {
                netHandler.disconnect(net.minecraft.network.chat.Component.empty());
            }
        });
    }

}
