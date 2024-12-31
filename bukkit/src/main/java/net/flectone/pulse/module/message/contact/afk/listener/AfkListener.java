package net.flectone.pulse.module.message.contact.afk.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.contact.afk.AfkModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@Singleton
public class AfkListener implements Listener {

    private final FPlayerManager fPlayerManager;
    private final AfkModule afkModule;

    @Inject
    public AfkListener(FPlayerManager fPlayerManager,
                       AfkModule afkModule) {
        this.fPlayerManager = fPlayerManager;
        this.afkModule = afkModule;
    }

    @EventHandler
    public void asyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        FPlayer fPlayer = fPlayerManager.get(event.getPlayer());

        afkModule.remove("chat", fPlayer);
    }

    @EventHandler
    public void playerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        FPlayer fPlayer = fPlayerManager.get(event.getPlayer());

        String message = event.getMessage();
        if (!message.isEmpty()) {
            message = message.split(" ")[0].substring(1);
        }

        afkModule.remove(message, fPlayer);
    }
}
