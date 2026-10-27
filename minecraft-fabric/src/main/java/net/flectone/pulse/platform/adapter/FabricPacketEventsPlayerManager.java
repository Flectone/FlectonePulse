package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.manager.AbstractFabricPlayerManager;
import net.flectone.pulse.FabricFlectonePulse;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

@Singleton
public class FabricPacketEventsPlayerManager extends AbstractFabricPlayerManager {

    private final FabricFlectonePulse fabricFlectonePulse;
    private final FabricVersionAdapter fabricVersionAdapter;

    @Inject
    public FabricPacketEventsPlayerManager(FabricFlectonePulse fabricFlectonePulse,
                                           FabricVersionAdapter fabricVersionAdapter) {
        super(PacketEvents.getAPI());

        this.fabricFlectonePulse = fabricFlectonePulse;
        this.fabricVersionAdapter = fabricVersionAdapter;
    }

    @Override
    public int getPing(@NonNull Object player) {
        return fabricVersionAdapter.getPing((ServerPlayer) player);
    }

    @Override
    public Object getChannel(@NonNull Object player) {
        return fabricVersionAdapter.getChannel((ServerPlayer) player);
    }

    @Override
    public void disconnectPlayer(@NonNull Object player, @NonNull String message) {
        fabricVersionAdapter.disconnect((ServerPlayer) player, message);
    }

    @Override
    public void kickOnException(@NonNull Object player, @NonNull String message) {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return;

        minecraftServer.execute(() -> disconnectPlayer(player, message));
    }

}
