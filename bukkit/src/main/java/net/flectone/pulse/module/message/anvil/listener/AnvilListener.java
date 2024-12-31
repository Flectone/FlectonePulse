package net.flectone.pulse.module.message.anvil.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.anvil.BukkitAnvilModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Singleton
public class AnvilListener implements Listener {

    private final FPlayerManager fPlayerManager;
    private final BukkitAnvilModule anvilModule;

    @Inject
    public AnvilListener(FPlayerManager fPlayerManager,
                         BukkitAnvilModule anvilModule) {
        this.fPlayerManager = fPlayerManager;
        this.anvilModule = anvilModule;
    }

    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getClickedInventory() instanceof AnvilInventory)) return;
        if (event.getSlot() != 2) return;
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getItemMeta() == null) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        FPlayer fPlayer = fPlayerManager.get(player);

        ItemStack itemStack = event.getCurrentItem();

        ItemMeta itemMeta = itemStack.getItemMeta();
        anvilModule.format(fPlayer, itemMeta);

        itemStack.setItemMeta(itemMeta);
    }
}
