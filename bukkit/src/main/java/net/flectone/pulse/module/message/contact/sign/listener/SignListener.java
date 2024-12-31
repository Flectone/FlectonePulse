package net.flectone.pulse.module.message.contact.sign.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.contact.sign.SignModule;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;

@Singleton
public class SignListener implements Listener {

    private final FPlayerManager fPlayerManager;
    private final SignModule signModule;

    @Inject
    public SignListener(FPlayerManager fPlayerManager,
                        SignModule signModule) {
        this.fPlayerManager = fPlayerManager;
        this.signModule = signModule;
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        FPlayer fPlayer = fPlayerManager.get(player);

        PlayerInventory inventory = player.getInventory();

        if (inventory.getItemInMainHand().getType() == Material.AIR) return;
        if (!inventory.getItemInOffHand().getType().toString().contains("DYE")) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        signModule.sign(fPlayer, clickedBlock);
    }
}
