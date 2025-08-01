package net.flectone.pulse.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.dispatcher.EventDispatcher;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.event.player.PlayerPersistAndDisposeEvent;
import net.flectone.pulse.service.FPlayerService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Singleton
public class FabricBaseListener {

    private final FPlayerService fPlayerService;
    private final EventDispatcher eventDispatcher;

    @Inject
    public FabricBaseListener(FPlayerService fPlayerService,
                              EventDispatcher eventDispatcher) {
        this.fPlayerService = fPlayerService;
        this.eventDispatcher = eventDispatcher;
    }

    @Async
    public void asyncProcessJoinEvent(ServerPlayNetworkHandler handler, PacketSender packetSender, MinecraftServer minecraftServer) {
        ServerPlayerEntity player = handler.getPlayer();
        FPlayer fPlayer = fPlayerService.getFPlayer(player.getUuid());

        eventDispatcher.dispatch(new PlayerLoadEvent(fPlayer));
        eventDispatcher.dispatch(new net.flectone.pulse.model.event.player.PlayerJoinEvent(fPlayer));
    }

    @Async
    public void asyncProcessQuitEvent(ServerPlayNetworkHandler handler, MinecraftServer minecraftServer) {
        ServerPlayerEntity player = handler.getPlayer();
        FPlayer fPlayer = fPlayerService.getFPlayer(player.getUuid());

        eventDispatcher.dispatch(new net.flectone.pulse.model.event.player.PlayerQuitEvent(fPlayer));
        eventDispatcher.dispatch(new PlayerPersistAndDisposeEvent(fPlayer));
    }

}
