package net.flectone.pulse.util;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.platform.provider.PacketProvider;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EntityUtil {

    private final PacketProvider packetProvider;
    
    public String resolveEntityTranslationKey(String entityType) {
        ItemType itemType = ItemTypes.getByName(entityType);

        if (itemType == null) {
            return "entity.minecraft." + entityType;
        }

        if (itemType.getPlacedType() == null) {
            return "item.minecraft." + entityType;
        }

        return "block.minecraft." + entityType;
    }

    public int entityOffset() {
        return 8;
    }

    public int textDisplayOffset() {
        return displayOffset() + 14;
    }

    public int displayOffset() {
        return entityOffset() + (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_20_2) ? 1 : 0);
    }

    public int areaEffectCloudRadiusIndex() {
        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_17)) {
            return 8;
        } else if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_14)) {
            return 7;
        } else {
            return 6;
        }
    }

}
