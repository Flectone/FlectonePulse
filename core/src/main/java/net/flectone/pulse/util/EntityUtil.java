package net.flectone.pulse.util;

import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EntityUtil {
    
    
    @Inject
    public EntityUtil() {
    }
    
    public String resolveEntityTranslationKey(String entityType) {
        ItemType itemType = ItemTypes.getByName(entityType);

        return itemType == null
                ? "entity.minecraft." + entityType
                : itemType.getPlacedType() == null ? "item.minecraft." + entityType : "block.minecraft." + entityType;
    }
    
}
