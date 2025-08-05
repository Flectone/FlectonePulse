package net.flectone.pulse.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

@UtilityClass
public class PaperItemStackUtil {

    public Map<Key, DataComponentValue> getDataComponents(ItemStack itemStack) {
        HoverEvent.ShowItem showItem = itemStack.asHoverEvent().value();
        return showItem.dataComponents();
    }

}
