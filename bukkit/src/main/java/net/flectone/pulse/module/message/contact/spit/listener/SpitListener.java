package net.flectone.pulse.module.message.contact.spit.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.contact.spit.SpitModule;
import org.bukkit.Material;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@Singleton
public class SpitListener implements Listener {

    private final FPlayerManager fPlayerManager;
    private final SpitModule spitModule;

    @Inject
    public SpitListener(FPlayerManager fPlayerManager,
                        SpitModule spitModule) {
        this.fPlayerManager = fPlayerManager;
        this.spitModule = spitModule;
    }

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (!action.equals(Action.RIGHT_CLICK_AIR)) return;

        Player player = event.getPlayer();
        FPlayer fPlayer = fPlayerManager.get(player);

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        itemStack = itemStack.getType().equals(Material.AIR) ? player.getInventory().getItemInOffHand() : itemStack;

        String targetItem = spitModule.getMessage().getItem();
        if (!itemStack.getType().toString().contains(targetItem)) return;

        spitModule.send(fPlayer, player.getLocation());
    }

    @EventHandler
    public void projectileHitEvent(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof LlamaSpit llamaSpit)) return;
        if (!(event.getEntity().getShooter() instanceof Player sender)) return;

        FPlayer fPlayer = fPlayerManager.get(sender);

        if (llamaSpit.getCustomName() == null) return;
        if (!llamaSpit.getCustomName().equals(spitModule.getSPIT_NAME())) return;

        event.setCancelled(true);

        if (!(event.getHitEntity() instanceof Player receiver)) return;

        FPlayer fReceiver = fPlayerManager.get(receiver);

        event.getEntity().remove();
        spitModule.send(fPlayer, fReceiver);
    }
}
