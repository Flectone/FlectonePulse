package net.flectone.pulse.module.message.contact.knock.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.contact.knock.KnockModule;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

@Singleton
public class KnockListener implements Listener {

    private final FPlayerManager fPlayerManager;
    private final KnockModule knockModule;

    @Inject
    public KnockListener(FPlayerManager fPlayerManager,
                         KnockModule knockModule) {
        this.fPlayerManager = fPlayerManager;
        this.knockModule = knockModule;
    }

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        FPlayer fPlayer = fPlayerManager.get(player);

        Block block = event.getClickedBlock();
        if (block == null) return;

        Location location = block.getLocation();
        location.setX(location.getX() + 0.5);
        location.setZ(location.getZ() + 0.5);

        knockModule.knock(fPlayer, location, block);
    }
}
