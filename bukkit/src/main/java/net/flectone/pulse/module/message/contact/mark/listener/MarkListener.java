package net.flectone.pulse.module.message.contact.mark.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.contact.mark.MarkModule;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@Singleton
public class MarkListener implements Listener {

    private final FPlayerManager fPlayerManager;
    private final MarkModule markModule;

    @Inject
    public MarkListener(FPlayerManager fPlayerManager,
                        MarkModule markModule) {
        this.fPlayerManager = fPlayerManager;
        this.markModule = markModule;
    }

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (!action.equals(Action.RIGHT_CLICK_AIR) && !action.equals(Action.RIGHT_CLICK_BLOCK)) return;

        String targetItem = markModule.getMessage().getItem().toLowerCase();

        Player player = event.getPlayer();
        FPlayer fPlayer = fPlayerManager.get(player);

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        itemStack = itemStack.getType().equals(Material.AIR) ? player.getInventory().getItemInOffHand() : itemStack;

        if (!itemStack.getType().toString().toLowerCase().contains(targetItem)) return;

        String color = itemStack.getItemMeta() != null
                ? itemStack.getItemMeta().getDisplayName().trim().toLowerCase()
                : "white";

        markModule.mark(fPlayer, NamedTextColor.NAMES.valueOr(color, NamedTextColor.WHITE));
    }
}
