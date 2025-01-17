package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.model.FPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Singleton
public class BukkitItemUtil implements ItemUtil {

    private final ServerUtil bukkitUtil;
    private final NamespacedKey signKey;

    @Inject
    public BukkitItemUtil(ServerUtil bukkitUtil,
                          @Named("flectonepulseSign") NamespacedKey signKey) {
        this.bukkitUtil = bukkitUtil;
        this.signKey = signKey;
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

    @Override
    public void decreaseItemAmount(Object item, Runnable replaceItem) {
        if (!(item instanceof ItemStack itemStack)) return;

        if (itemStack.getAmount() == 1) {
            replaceItem.run();
        } else {
            itemStack.setAmount(itemStack.getAmount() - 1);
        }
    }

    @Sync
    @Override
    public void dropItem(Object location, Object item) {
        if (!(location instanceof Location bukkitLocation)) return;
        if (!(item instanceof ItemStack itemStack)) return;

        World world = bukkitLocation.getWorld();
        if (world == null) return;

        world.dropItem(bukkitLocation, itemStack);
    }

    @Override
    public void removeSignIndex(Object itemMeta) {
        if (itemMeta instanceof ItemMeta bukkitItemMeta) {
            bukkitItemMeta.getPersistentDataContainer().remove(signKey);
        }
    }

    @Override
    public void setSignIndex(Object itemMeta, int[] signIndexes) {
        if (itemMeta instanceof ItemMeta bukkitItemMeta) {
            bukkitItemMeta.getPersistentDataContainer().set(signKey, PersistentDataType.INTEGER_ARRAY, signIndexes);
        }
    }

    @Override
    public Pair<Integer, int[]> findSignIndex(Object itemMeta, List<String> itemLore, FPlayer fPlayer) {
        if (!(itemMeta instanceof ItemMeta bukkitItemMeta)) return new Pair<>(-1, new int[]{});

        int index = -1;
        int[] signIndexes = bukkitItemMeta.getPersistentDataContainer().get(signKey, PersistentDataType.INTEGER_ARRAY);
        if (signIndexes != null) {
            for (int signIndex : signIndexes) {
                if (signIndex > itemLore.size() - 1) continue;

                String loreString = ChatColor.stripColor(itemLore.get(signIndex));
                if (loreString.contains(fPlayer.getName())) {
                    index = signIndex;
                    break;
                }
            }
        } else signIndexes = new int[]{};

        return new Pair<>(index, signIndexes);
    }
}
