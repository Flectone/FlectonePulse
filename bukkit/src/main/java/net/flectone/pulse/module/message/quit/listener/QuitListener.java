package net.flectone.pulse.module.message.quit.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.quit.QuitModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@Singleton
public class QuitListener implements Listener {

    private final QuitModule quitModule;

    @Inject
    public QuitListener(QuitModule quitModule) {
        this.quitModule = quitModule;
    }

    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        if (!quitModule.isEnable()) return;

        event.setQuitMessage(null);
    }
}
