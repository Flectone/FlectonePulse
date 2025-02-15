package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Singleton
public class BukkitItemUtil implements ItemUtil {

    private final ServerUtil bukkitUtil;

    @Inject
    public BukkitItemUtil(ServerUtil bukkitUtil) {
        this.bukkitUtil = bukkitUtil;
    }

    // don't work after 0.2.0 release
    // need fix but idk how
    // more information https://discord.com/channels/861147957365964810/1329866516732182579
    @Nullable
    @Override
    public HoverEvent<?> hoverEvent(Object item) {
        if (!(item instanceof ItemStack itemStack)) return null;
        if (itemStack.getType() == Material.AIR) return null;

        try {
            return ((HoverEventSource<?>) itemStack).asHoverEvent();
        } catch (ClassCastException ignored) {}

        return null;
    }

    @Override
    public Component translatableComponent(Object item) {
        if (!(item instanceof ItemStack itemStack)) return Component.empty();

        HoverEvent<?> hoverEvent = hoverEvent(itemStack);

        Component component = itemStack.getItemMeta() == null || itemStack.getItemMeta().getDisplayName().isEmpty()
                ? Component.translatable(bukkitUtil.getMinecraftName(itemStack))
                : Component.text(itemStack.getItemMeta().getDisplayName()).decorate(TextDecoration.ITALIC);

        if (hoverEvent != null) {
            component = component.hoverEvent(hoverEvent);
        }

        return component;
    }
}
