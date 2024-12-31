package net.flectone.pulse.module.command.spy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEditBookEvent;

@Singleton
public class SpyListener implements Listener {

    private final BukkitSpyModule spyModule;

    @Inject
    public SpyListener(BukkitSpyModule spyModule) {
        this.spyModule = spyModule;
    }

    @EventHandler
    public void playerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        spyModule.check(event);
    }

    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event) {
        spyModule.check(event);
    }

    @EventHandler
    public void playerEditBookEvent(PlayerEditBookEvent event) {
        spyModule.check(event);
    }

    @EventHandler
    public void signChangeEvent(SignChangeEvent event) {
        spyModule.check(event);
    }

    @EventHandler
    public void asyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        spyModule.check(event);
    }
}
