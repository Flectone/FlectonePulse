package net.flectone.pulse.module.message.contact.mark.model;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.model.FPacketEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public class FMark extends FPacketEntity {

    private final int range;
    private final int duration;
    private final EntityType entityType;

    private Entity entity;

    public FMark(int range, int duration, String entityType) {
        this.range = range;
        this.duration = duration;
        this.entityType = SpigotConversionUtil.fromBukkitEntityType(org.bukkit.entity.EntityType.valueOf(entityType));
    }

    public void create(FPlayerManager fPlayerManager, Location location, RandomUtil randomUtil) {
        id = randomUtil.nextInt(Integer.MAX_VALUE);
        uuid = UUID.randomUUID();

        World world = location.getWorld();
        if (world == null) return;

        viewers.addAll(world.getPlayers().stream().map(fPlayerManager::get).toList());
        alive = true;
    }

    public void create(FPlayerManager fPlayerManager, Entity entity) {
        id = entity.getEntityId();
        uuid = entity.getUniqueId();
        this.entity = entity;
        viewers.addAll(entity.getWorld().getPlayers().stream().map(fPlayerManager::get).toList());
        alive = true;
    }

    public void setGlowing(Location location) {
        if (entityType == null) return;

        sendPacketToViewers(new WrapperPlayServerSpawnEntity(id, uuid,
                entityType,
                SpigotConversionUtil.fromBukkitLocation(location),
                0,
                0,
                null)
        );

        // invisible and glowing
        List<EntityData> metadata = Collections.singletonList(new EntityData(0, EntityDataTypes.BYTE, (byte) 96));
        sendPacketToViewers(new WrapperPlayServerEntityMetadata(id, metadata));
    }

    public void setGlowing(boolean value) {
        byte mask = value ? (byte) 0x40 : 0;
        List<EntityData> metadata = Collections.singletonList(new EntityData(0, EntityDataTypes.BYTE, mask));
        sendPacketToViewers(new WrapperPlayServerEntityMetadata(id, metadata));
    }

    public void remove() {
        alive = false;

        if (entity != null && !entity.isValid()) return;

        sendPacketToViewers(new WrapperPlayServerDestroyEntities(id));
    }
}
