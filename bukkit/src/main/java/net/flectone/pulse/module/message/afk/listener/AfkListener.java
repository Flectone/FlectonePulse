package net.flectone.pulse.module.message.afk.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.service.FPlayerService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@Singleton
public class AfkListener implements Listener {

    private final FPlayerService fPlayerService;
    private final AfkModule afkModule;

    @Inject
    public AfkListener(FPlayerService fPlayerService,
                       AfkModule afkModule) {
        this.fPlayerService = fPlayerService;
        this.afkModule = afkModule;
    }

    @EventHandler
    public void asyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer());



        afkModule.remove("chat", fPlayer);
    }

    @EventHandler
    public void playerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer());

        String message = event.getMessage();
        if (!message.isEmpty()) {
            message = message.split(" ")[0].substring(1);
        }

        afkModule.remove(message, fPlayer);
    }
}
