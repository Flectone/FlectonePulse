package net.flectone.pulse.module.message.join.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.platform.controller.ModuleController;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class JoinListener implements Listener {

    private final ModuleController moduleController;

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        if (!moduleController.isEnable(JoinModule.class)) return;

        event.setJoinMessage(null);
    }
}
