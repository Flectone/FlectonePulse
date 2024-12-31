package net.flectone.pulse.module.message.contact.unsign.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.module.message.contact.unsign.UnsignModule;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@Singleton
public class UnsignListener implements Listener {

    private final FPlayerManager fPlayerManager;
    private final UnsignModule unsignModule;

    @Inject
    public UnsignListener(FPlayerManager fPlayerManager,
                          UnsignModule unsignModule) {
        this.fPlayerManager = fPlayerManager;
        this.unsignModule = unsignModule;
    }

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();

        ItemStack usedItem = inventory.getItemInMainHand().getType() == Material.AIR
                ? inventory.getItemInOffHand()
                : inventory.getItemInMainHand();

        if (usedItem.getType() == Material.AIR) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        unsignModule.unSign(fPlayerManager.get(player), usedItem, clickedBlock);
    }
}
