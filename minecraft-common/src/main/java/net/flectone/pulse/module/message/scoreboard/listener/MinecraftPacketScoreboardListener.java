package net.flectone.pulse.module.message.scoreboard.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.message.scoreboard.MinecraftScoreboardModule;

import java.util.Collection;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPacketScoreboardListener implements PacketListener {

    private final MinecraftScoreboardModule scoreboardModule;
    
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.TEAMS) {
            WrapperPlayServerTeams wrapper = new WrapperPlayServerTeams(event);

            Collection<String> owners = wrapper.getPlayers();
            if (owners.isEmpty()) return;

            UUID receiver = event.getUser().getUUID();
            owners.forEach(owner -> scoreboardModule.forgetTeam(receiver, owner));
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY) {
            if (scoreboardModule.config().nameVisible()) return;

            WrapperPlayServerSpawnEntity wrapperPlayServerSpawnEntity = new WrapperPlayServerSpawnEntity(event);
            if (!wrapperPlayServerSpawnEntity.getEntityType().isInstanceOf(EntityTypes.PLAYER)) return;

            UUID uuid = event.getUser().getUUID();
            if (!scoreboardModule.isModernPlayer(uuid)) return;

            int entityId = wrapperPlayServerSpawnEntity.getEntityId();
            event.getTasksAfterSend().add(() -> scoreboardModule.send(uuid, entityId, false));
        }
    }
    
}
