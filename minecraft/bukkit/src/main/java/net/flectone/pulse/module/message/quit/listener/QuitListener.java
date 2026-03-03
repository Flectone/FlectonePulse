package net.flectone.pulse.module.message.quit.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.platform.controller.ModuleController;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class QuitListener implements Listener {

    private final ModuleController moduleController;

    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        if (!moduleController.isEnable(QuitModule.class)) return;

        event.setQuitMessage(null);
    }
}
