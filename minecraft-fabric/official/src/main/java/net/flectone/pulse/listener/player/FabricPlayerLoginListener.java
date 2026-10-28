package net.flectone.pulse.listener.player;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.platform.adapter.FabricVersionAdapter;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.serializer.FabricComponentSerializer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricPlayerLoginListener {

    private final FileFacade fileFacade;
    private final MinecraftPacketProvider packetProvider;
    private final PlayerPreLoginProcessor playerPreLoginProcessor;
    private final FabricComponentSerializer componentSerializer;
    private final FabricVersionAdapter fabricVersionAdapter;

    public void onPreLogin(ServerLoginPacketListenerImpl netHandler) {
        if (!fabricVersionAdapter.isAcceptingMessages(netHandler)) return;

        // in older versions (1.20.1 and older), there is no configuration stage
        if (fileFacade.config().internal().usePacketLoginListener()
                && packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_20_2)) return;

        GameProfile profile = fabricVersionAdapter.getProfile(netHandler);
        if (profile == null) return;

        UUID uuid = fabricVersionAdapter.getId(profile);
        String name = fabricVersionAdapter.getName(profile);

        playerPreLoginProcessor.processLogin(uuid, name, loginEvent -> {
            try {
                netHandler.disconnect(componentSerializer.toFabric(loginEvent.kickReason()));
            } catch (Throwable _) {
                netHandler.disconnect(fabricVersionAdapter.literal(""));
            }
        });
    }


}