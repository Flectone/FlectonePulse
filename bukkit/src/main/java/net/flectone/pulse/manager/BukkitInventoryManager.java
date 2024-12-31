package net.flectone.pulse.manager;

import com.google.inject.Singleton;
import org.bukkit.Bukkit;

import java.util.UUID;

@Singleton
public class BukkitInventoryManager extends InventoryManager {

    @Override
    public void update(UUID uuid) {
        // remove this
        Bukkit.getPlayer(uuid).updateInventory();
    }
}
