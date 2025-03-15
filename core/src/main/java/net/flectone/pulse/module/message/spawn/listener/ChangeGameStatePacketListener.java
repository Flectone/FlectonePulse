package net.flectone.pulse.module.message.spawn.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.spawn.SpawnModule;
import net.flectone.pulse.util.MinecraftTranslationKeys;

@Singleton
public class ChangeGameStatePacketListener extends AbstractPacketListener {

    private final SpawnModule spawnModule;

    @Inject
    public ChangeGameStatePacketListener(SpawnModule spawnModule) {
        this.spawnModule = spawnModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() != PacketType.Play.Server.CHANGE_GAME_STATE) return;
        if (!spawnModule.isEnable()) return;

        WrapperPlayServerChangeGameState wrapper = new WrapperPlayServerChangeGameState(event);
        if (wrapper.getReason() != WrapperPlayServerChangeGameState.Reason.NO_RESPAWN_BLOCK_AVAILABLE) return;

        event.setCancelled(true);
        spawnModule.send(event.getUser().getUUID(), MinecraftTranslationKeys.BLOCK_MINECRAFT_SPAWN_NOT_VALID);
    }
}

