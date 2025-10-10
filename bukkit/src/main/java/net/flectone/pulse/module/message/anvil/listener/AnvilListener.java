package net.flectone.pulse.module.message.anvil.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.anvil.BukkitAnvilModule;
import net.flectone.pulse.service.FPlayerService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AnvilListener implements Listener {

    private final FPlayerService fPlayerService;
    private final BukkitAnvilModule anvilModule;

    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getClickedInventory() instanceof AnvilInventory)) return;
        if (event.getSlot() != 2) return;
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getItemMeta() == null) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(player);

        ItemStack itemStack = event.getCurrentItem();
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (anvilModule.format(fPlayer, itemMeta)) {
            itemStack.setItemMeta(itemMeta);
        }
    }
}
