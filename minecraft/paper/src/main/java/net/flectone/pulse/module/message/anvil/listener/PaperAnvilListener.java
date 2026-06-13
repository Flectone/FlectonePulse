package net.flectone.pulse.module.message.anvil.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.anvil.AnvilModule;
import net.flectone.pulse.processing.PaperComponentSerializer;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PaperAnvilListener implements Listener {

    private final FPlayerService fPlayerService;
    private final AnvilModule anvilModule;
    private final PaperComponentSerializer paperComponentSerializer;

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getClickedInventory() instanceof AnvilInventory)) return;
        if (event.getSlot() != 2) return;

        ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null) return;

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemMeta.hasDisplayName()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(player.getUniqueId());

        try {
            // try paper format
            Component componentDisplayName = itemMeta.displayName();
            if (componentDisplayName == null) return;

            String displayName = paperComponentSerializer.toPlain(componentDisplayName).orElse(itemMeta.getDisplayName());

            // skip empty string
            if (StringUtils.isEmpty(displayName)) return;

            anvilModule.paperFormat(fPlayer, displayName)
                    .flatMap(paperComponentSerializer::fromJson)
                    .ifPresent(itemMeta::displayName);
        } catch (Exception _) {
            // use deprecated format
            anvilModule.legacyFormat(fPlayer, itemMeta.getDisplayName())
                    .ifPresent(itemMeta::setDisplayName);
        }

        itemStack.setItemMeta(itemMeta);
    }
}
