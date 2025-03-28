package net.flectone.pulse.module.message.chat.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.chat.BukkitChatModule;
import net.flectone.pulse.service.FPlayerService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@Singleton
public class ChatListener implements Listener {

    private final FPlayerService fPlayerService;
    private final BukkitChatModule chatModule;

    @Inject
    public ChatListener(FPlayerService fPlayerService,
                        BukkitChatModule chatModule) {
        this.fPlayerService = fPlayerService;
        this.chatModule = chatModule;
    }

    @EventHandler
    public void asyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        if (event.getRecipients().isEmpty()) return;
        if (!event.getFormat().equals("<%1$s> %2$s")) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer());
        if (!chatModule.isEnable()) return;

        chatModule.send(fPlayer, event);
    }
}
