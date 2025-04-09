package net.flectone.pulse.module.message.mark.model;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.model.FPacketEntity;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.RandomUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public class FMark extends FPacketEntity {

    private final int range;
    private final int duration;
    private final Message.Mark.Legacy legacy;
    private final Message.Mark.Modern modern;

    private Entity entity;

    public FMark(int range,
                 int duration,
                 Message.Mark.Legacy legacy,
                 Message.Mark.Modern modern) {
        this.range = range;
        this.duration = duration;
        this.legacy = legacy;
        this.modern = modern;
    }

    public void create(FPlayerService fPlayerService, Location location, RandomUtil randomUtil) {
        id = randomUtil.nextInt(Integer.MAX_VALUE);
        uuid = UUID.randomUUID();

        World world = location.getWorld();
        if (world == null) return;

        viewers.addAll(world.getPlayers()
                .stream()
                .filter(receiver -> receiver.getWorld().equals(world))
                .filter(receiver -> receiver.getLocation().distance(location) <= 100.0)
                .map(fPlayerService::getFPlayer)
                .toList()
        );

        alive = true;
    }

    public void create(FPlayerService fPlayerService, Entity entity) {
        id = entity.getEntityId();
        uuid = entity.getUniqueId();
        this.entity = entity;
        viewers.addAll(entity.getWorld().getPlayers()
                .stream()
                .filter(receiver -> receiver.getWorld().equals(entity.getWorld()))
                .filter(receiver -> receiver.getLocation().distance(entity.getLocation()) <= 100.0)
                .map(fPlayerService::getFPlayer)
                .toList()
        );
        alive = true;
    }

    public void setGlowing(Location location) {
        EntityType entityType = null;
        if (legacy.isEnable() && !modern.isEnable()) {
            try {
                entityType = SpigotConversionUtil.fromBukkitEntityType(org.bukkit.entity.EntityType.valueOf(legacy.getEntity()));
            } catch (IllegalArgumentException ignored) {}

        } else if (modern.isEnable() && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_4)) {
            entityType = EntityTypes.BLOCK_DISPLAY;
        }

        if (entityType == null) return;

        sendPacketToViewers(new WrapperPlayServerSpawnEntity(id, uuid,
                entityType,
                SpigotConversionUtil.fromBukkitLocation(location),
                0,
                0,
                null)
        );

        List<EntityData<?>> metadata = new ArrayList<>();

        // size
        if (EntityTypes.MAGMA_CUBE.equals(entityType) || EntityTypes.SLIME.equals(entityType)) {
            metadata.add(new EntityData<>(16, EntityDataTypes.INT, legacy.getSize()));
        }

        if (EntityTypes.BLOCK_DISPLAY.equals(entityType)) {
            // scale
            float scale = modern.getScale();
            metadata.add(new EntityData<>(12, EntityDataTypes.VECTOR3F, new Vector3f(scale, scale, scale)));

            // block state
            int blockId = 0;
            try {
                blockId = SpigotConversionUtil.fromBukkitBlockData(Material.valueOf(modern.getBlock()).createBlockData()).getGlobalId();
            } catch (IllegalArgumentException ignored) {}

            metadata.add(new EntityData<>(23, EntityDataTypes.BLOCK_STATE, blockId));
        }

        // invisible and glowing
        metadata.add(new EntityData<>(0, EntityDataTypes.BYTE, (byte) 96));

        sendPacketToViewers(new WrapperPlayServerEntityMetadata(id, metadata));
    }

    public void setGlowing(boolean value) {
        byte mask = value ? (byte) 0x40 : 0;
        List<EntityData<?>> metadata = Collections.singletonList(new EntityData<>(0, EntityDataTypes.BYTE, mask));
        sendPacketToViewers(new WrapperPlayServerEntityMetadata(id, metadata));
    }

    public void remove() {
        alive = false;

        if (entity != null && !entity.isValid()) return;

        sendPacketToViewers(new WrapperPlayServerDestroyEntities(id));
    }
}
