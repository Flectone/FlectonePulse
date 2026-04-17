package net.flectone.pulse.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.event.player.PlayerPersistAndDisposeEvent;
import net.flectone.pulse.service.FPlayerService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricBaseListener {

    private final Set<UUID> joinedPlayers = new CopyOnWriteArraySet<>();

    private final FPlayerService fPlayerService;
    private final EventDispatcher eventDispatcher;
    private final TaskScheduler taskScheduler;

    public void asyncProcessJoinEvent(ServerGamePacketListenerImpl handler, PacketSender packetSender, MinecraftServer minecraftServer) {
        ServerPlayer player = handler.getPlayer();
        UUID playerUUID = player.getUUID();
        joinedPlayers.add(playerUUID);

        taskScheduler.runAsyncLater(() -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(playerUUID);

            eventDispatcher.dispatch(new PlayerLoadEvent(fPlayer));
            eventDispatcher.dispatch(new net.flectone.pulse.model.event.player.PlayerJoinEvent(fPlayer));
        }, 1L);
    }

    public void asyncProcessQuitEvent(ServerGamePacketListenerImpl handler, MinecraftServer minecraftServer) {
        ServerPlayer player = handler.getPlayer();
        UUID playerUUID = player.getUUID();
        if (!joinedPlayers.remove(playerUUID)) return;

        taskScheduler.runAsync(() -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(playerUUID);

            eventDispatcher.dispatch(new net.flectone.pulse.model.event.player.PlayerQuitEvent(fPlayer));
            eventDispatcher.dispatch(new PlayerPersistAndDisposeEvent(fPlayer));
        });
    }

}
