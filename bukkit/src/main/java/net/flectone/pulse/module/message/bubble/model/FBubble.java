package net.flectone.pulse.module.message.bubble.model;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.model.FPacketEntity;
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

    public FBubble(long duration, FPlayer fPlayer, FPlayer fReceiver, Component text) {
        this(duration, 0, 0, fPlayer, fReceiver, text, EntityTypes.AREA_EFFECT_CLOUD);
    }

    public FBubble(long duration, int lineWidth, FPlayer fPlayer, FPlayer fReceiver, Component text) {
        this(duration, lineWidth, 0, fPlayer, fReceiver, text, EntityTypes.TEXT_DISPLAY);
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

        id = randomUtil.nextInt(Integer.MAX_VALUE);
        uuid = UUID.randomUUID();
        viewers.add(fReceiver);
        alive = true;

        sendPacketToViewers(new WrapperPlayServerSpawnEntity(id, uuid,
                type,
                SpigotConversionUtil.fromBukkitLocation(player.getLocation()),
                0,
                0,
                null)
        );

        List<EntityData> metadataList = new ArrayList<>();

        if (type == EntityTypes.INTERACTION) {
            // width
            metadataList.add(new EntityData(8, EntityDataTypes.FLOAT, (float) 0.000001));
            // height
            metadataList.add(new EntityData(9, EntityDataTypes.FLOAT, height));
        } else if (type == EntityTypes.TEXT_DISPLAY) {
            // center for viewer
            metadataList.add(new EntityData(15, EntityDataTypes.BYTE, (byte) 3));
            // text
            metadataList.add(new EntityData(23, EntityDataTypes.ADV_COMPONENT, text));
            // width
            metadataList.add(new EntityData(24, EntityDataTypes.INT, lineWidth));
        } else {
            // text
            metadataList.add(new EntityData(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(text)));
            // custom name visible
            metadataList.add(new EntityData(3, EntityDataTypes.BOOLEAN, true));
            // radius
            metadataList.add(new EntityData(8, EntityDataTypes.FLOAT, (float) 0));
        }

        sendPacketToViewers(new WrapperPlayServerEntityMetadata(id, metadataList));
    }

    public void remove() {
        sendPacketToViewers(new WrapperPlayServerDestroyEntities(id));
        alive = false;
    }
}
