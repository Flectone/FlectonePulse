package net.flectone.pulse.controller;

import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Singleton
public class BukkitInventoryController extends InventoryController {

    @Override
    public void update(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        player.updateInventory();
    }
}
