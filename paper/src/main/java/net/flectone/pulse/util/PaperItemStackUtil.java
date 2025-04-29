package net.flectone.pulse.util;

import com.google.gson.JsonElement;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.inventory.ItemStack;

@UtilityClass
public class PaperItemStackUtil {

    public JsonElement serialize(ItemStack itemStack) {
        Component component = Component.text(itemStack.getType().name());

        return GsonComponentSerializer.gson().serializeToTree(component.hoverEvent(itemStack));
    }

}
