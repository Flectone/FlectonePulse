package net.flectone.pulse.listener.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.flectone.pulse.mixin.ServerLoginPacketListenerImplAccessor;
import net.flectone.pulse.processing.processor.PlayerPreLoginProcessor;
import net.flectone.pulse.processing.serializer.FabricComponentSerializer;
import net.flectone.pulse.util.file.FileFacade;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricPlayerLoginListener {

    private final FileFacade fileFacade;
    private final PlayerPreLoginProcessor playerPreLoginProcessor;
    private final FabricComponentSerializer componentSerializer;

    public void onPreLogin(ServerLoginPacketListenerImpl netHandler, MinecraftServer server, PacketSender packetSender, ServerLoginNetworking.LoginSynchronizer synchronizer) {
        if (fileFacade.config().internal().usePacketLoginListener()) return;
        if (!netHandler.isAcceptingMessages()) return;

        GameProfile profile = ((ServerLoginPacketListenerImplAccessor) netHandler).getAuthenticatedProfile();
        UUID uuid = profile.id();
        String name = profile.name();

        playerPreLoginProcessor.processLogin(uuid, name, loginEvent -> {
            try {
                netHandler.disconnect(componentSerializer.toFabric(loginEvent.kickReason()));
            } catch (Throwable _) {
                netHandler.disconnect(Component.empty());
            }
        });
    }


}