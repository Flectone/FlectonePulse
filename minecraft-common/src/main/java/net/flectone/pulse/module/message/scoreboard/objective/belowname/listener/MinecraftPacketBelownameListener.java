package net.flectone.pulse.module.message.scoreboard.objective.belowname.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.message.scoreboard.objective.belowname.MinecraftBelownameModule;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPacketBelownameListener implements PacketListener {

    private final MinecraftBelownameModule belownameModule;

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.SPAWN_ENTITY) return;

        WrapperPlayServerSpawnEntity wrapperPlayServerSpawnEntity = new WrapperPlayServerSpawnEntity(event);
        if (!wrapperPlayServerSpawnEntity.getEntityType().isInstanceOf(EntityTypes.PLAYER)) return;

        UUID uuid = event.getUser().getUUID();
        if (!belownameModule.isModernPlayer(uuid)) return;

        int entityId = wrapperPlayServerSpawnEntity.getEntityId();
        event.getTasksAfterSend().add(() -> belownameModule.send(uuid, entityId, false));
    }

}
