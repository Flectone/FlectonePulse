package net.flectone.pulse;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import io.github.retrooper.packetevents.factory.fabric.FabricPacketEventsAPI;
import io.github.retrooper.packetevents.factory.fabric.FabricPlayerManager;
import io.github.retrooper.packetevents.factory.fabric.FabricServerManager;
import io.github.retrooper.packetevents.impl.netty.manager.player.PlayerManagerAbstract;
import lombok.experimental.UtilityClass;
import net.fabricmc.api.EnvType;
import net.minecraft.SharedConstants;

@UtilityClass
public class WrapperPacketEvents {

    public void load() {
        // configure packetevents api
        System.setProperty("packetevents.nbt.default-max-size", "2097152");
        PacketEvents.setAPI(new FabricPacketEventsAPI("packetevents", EnvType.SERVER) {
            @Override
            protected ServerManager constructServerManager() {
                SharedConstants.tryDetectVersion();
                String version = SharedConstants.getCurrentVersion().id();
                return new FabricServerManager(version);
            }

            @Override
            protected PlayerManagerAbstract constructPlayerManager() {
                return new FabricPlayerManager();
            }
        });
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false).checkForUpdates(false).debug(false);
        PacketEvents.getAPI().load();
    }

    public void init() {
        PacketEvents.getAPI().init();
    }

    public void terminateFailed() {
        try {
            PacketEventsAPI<?> packetEventsAPI = PacketEvents.getAPI();
            if (!packetEventsAPI.isInitialized()) {
                packetEventsAPI.getInjector().uninject();
            }
        } catch (Exception _) {
            // ignore
        }
    }

    public void terminate() {
        PacketEvents.getAPI().terminate();
    }

}
