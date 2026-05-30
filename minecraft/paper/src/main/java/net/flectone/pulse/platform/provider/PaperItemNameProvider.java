package net.flectone.pulse.platform.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PaperItemNameProvider {

    @Nullable
    public String get(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return null;
        if (!itemMeta.hasDisplayName()) return null;

        Component displayName = itemMeta.displayName();
        if (displayName == null) return null;

        return GsonComponentSerializer.gson().serialize(displayName);
    }

}
