package net.flectone.pulse.module.message.join.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.join.JoinModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@Singleton
public class JoinListener implements Listener {

    private final JoinModule joinModule;

    @Inject
    public JoinListener(JoinModule joinModule) {
        this.joinModule = joinModule;
    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        if (!joinModule.isEnable()) return;

        event.setJoinMessage(null);
    }
}
