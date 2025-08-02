package net.flectone.pulse.module.message.spawn.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.spawn.SpawnModule;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.flectone.pulse.service.FPlayerService;

@Singleton
public class SpawnPacketListener implements PacketListener {

    private final SpawnModule spawnModule;
    private final FPlayerService fPlayerService;

    @Inject
    public SpawnPacketListener(SpawnModule spawnModule,
                               FPlayerService fPlayerService) {
        this.spawnModule = spawnModule;
        this.fPlayerService = fPlayerService;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() != PacketType.Play.Server.CHANGE_GAME_STATE) return;
        if (!spawnModule.isEnable()) return;

        WrapperPlayServerChangeGameState wrapper = new WrapperPlayServerChangeGameState(event);
        if (wrapper.getReason() != WrapperPlayServerChangeGameState.Reason.NO_RESPAWN_BLOCK_AVAILABLE) return;

        event.setCancelled(true);

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getUser().getUUID());
        spawnModule.send(fPlayer, MinecraftTranslationKey.BLOCK_MINECRAFT_SPAWN_NOT_VALID);
    }
}

