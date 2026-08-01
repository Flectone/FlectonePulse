package net.flectone.pulse.module.command.spy.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.command.spy.BukkitSpyModule;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitSpyListener implements Listener {

    private final BukkitSpyModule spyModule;

    @EventHandler
    public void asyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        spyModule.checkChat(event.getPlayer().getUniqueId(), event.getMessage(), event.getRecipients().stream()
                .map(Entity::getUniqueId)
                .toList()
        );
    }
}
