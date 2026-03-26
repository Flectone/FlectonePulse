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

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricBaseListener {

    private final FPlayerService fPlayerService;
    private final EventDispatcher eventDispatcher;
    private final TaskScheduler taskScheduler;

    public void asyncProcessJoinEvent(ServerGamePacketListenerImpl handler, PacketSender packetSender, MinecraftServer minecraftServer) {
        taskScheduler.runAsyncLater(() -> {
            ServerPlayer player = handler.getPlayer();
            FPlayer fPlayer = fPlayerService.getFPlayer(player.getUUID());

            eventDispatcher.dispatch(new PlayerLoadEvent(fPlayer));
            eventDispatcher.dispatch(new net.flectone.pulse.model.event.player.PlayerJoinEvent(fPlayer));
        }, 1L);
    }

    public void asyncProcessQuitEvent(ServerGamePacketListenerImpl handler, MinecraftServer minecraftServer) {
        taskScheduler.runAsync(() -> {
            ServerPlayer player = handler.getPlayer();
            FPlayer fPlayer = fPlayerService.getFPlayer(player.getUUID());

            eventDispatcher.dispatch(new net.flectone.pulse.model.event.player.PlayerQuitEvent(fPlayer));
            eventDispatcher.dispatch(new PlayerPersistAndDisposeEvent(fPlayer));
        });
    }

}
