package net.flectone.pulse.module.message.bubble.model;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import net.flectone.pulse.model.FPacketEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.RandomUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
public class FBubble extends FPacketEntity {

    private final long duration;
    private final float height;
    private final int lineWidth;
    private final FPlayer fPlayer;
    private final FPlayer fReceiver;
    private final Component text;
    private final EntityType type;

    private boolean visibleName = true;
    private boolean hasShadow;
    private int backgroundColor;
    private float scale;

    public FBubble(long duration, FPlayer fPlayer, FPlayer fReceiver, Component text) {
        this(duration, 0, 0, fPlayer, fReceiver, text, EntityTypes.AREA_EFFECT_CLOUD);
    }

    public FBubble(long duration, FPlayer fPlayer, FPlayer fReceiver, Component text, boolean visibleName) {
        this(duration, fPlayer, fReceiver, text);
        this.visibleName = visibleName;
    }

    public FBubble(boolean hasShadow, long duration, int lineWidth, int backgroundColor, float scale, FPlayer fPlayer, FPlayer fReceiver, Component text) {
        this(duration, lineWidth, 0, fPlayer, fReceiver, text, EntityTypes.TEXT_DISPLAY);
        this.hasShadow = hasShadow;
        this.backgroundColor = backgroundColor;
        this.scale = scale;
    }

    public FBubble(long duration, float height, FPlayer fPlayer, FPlayer fReceiver) {
        this(duration, 0, height, fPlayer, fReceiver, Component.empty(), EntityTypes.INTERACTION);
    }

    public FBubble(long duration, int lineWidth, float height, FPlayer fPlayer, FPlayer fReceiver, Component text, EntityType type) {
        this.duration = duration;
        this.lineWidth = lineWidth;
        this.height = height;
        this.fPlayer = fPlayer;
        this.fReceiver = fReceiver;
        this.text = text;
        this.type = type;
    }

    public void spawn(RandomUtil randomUtil) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        Location location = SpigotConversionUtil.fromBukkitLocation(player.getLocation());
        location.setPosition(location.getPosition().add(0, 1.8, 0));

        id = randomUtil.nextInt(Integer.MAX_VALUE);
        uuid = UUID.randomUUID();
        viewers.add(fReceiver);
        alive = true;

        sendPacketToViewers(new WrapperPlayServerSpawnEntity(id, uuid, type, location, 0, 0, null));

        List<EntityData> metadataList = new ArrayList<>();

        if (type == EntityTypes.INTERACTION) {
            // width
            metadataList.add(new EntityData(8, EntityDataTypes.FLOAT, (float) 0.000001));
            // height
            metadataList.add(new EntityData(9, EntityDataTypes.FLOAT, height));
        } else if (type == EntityTypes.TEXT_DISPLAY) {
            // scale
            metadataList.add(new EntityData(12, EntityDataTypes.VECTOR3F, new Vector3f(scale, scale, scale)));
            // center for viewer
            metadataList.add(new EntityData(15, EntityDataTypes.BYTE, (byte) 3));
            // text
            metadataList.add(new EntityData(23, EntityDataTypes.ADV_COMPONENT, text));
            // width
            metadataList.add(new EntityData(24, EntityDataTypes.INT, lineWidth));
            // background color
            metadataList.add(new EntityData(25, EntityDataTypes.INT, backgroundColor));

            if (hasShadow) {
                metadataList.add(new EntityData(27, EntityDataTypes.BYTE, (byte) 0x01));
            }

        } else {
            // text
            metadataList.add(new EntityData(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(text)));
            // custom name visible
            metadataList.add(new EntityData(3, EntityDataTypes.BOOLEAN, visibleName));

            // radius
            int radiusIndex = 8;

            if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_16_5)) {
                radiusIndex = 7;
            }

            metadataList.add(new EntityData(radiusIndex, EntityDataTypes.FLOAT, 0f));
        }

        sendPacketToViewers(new WrapperPlayServerEntityMetadata(id, metadataList));
    }

    public void remove() {
        sendPacketToViewers(new WrapperPlayServerDestroyEntities(id));
        alive = false;
    }
}
