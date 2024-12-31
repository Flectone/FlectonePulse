package net.flectone.pulse.module.message.chat.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.chat.BukkitChatModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@Singleton
public class ChatListener implements Listener {

    private final FPlayerManager fPlayerManager;
    private final BukkitChatModule chatModule;

    @Inject
    public ChatListener(FPlayerManager fPlayerManager,
                        BukkitChatModule chatModule) {
        this.fPlayerManager = fPlayerManager;
        this.chatModule = chatModule;
    }

    @EventHandler
    public void asyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        if (event.getRecipients().isEmpty()) return;
        if (!event.getFormat().equals("<%1$s> %2$s")) return;

        FPlayer fPlayer = fPlayerManager.get(event.getPlayer());
        if (!chatModule.isEnable()) return;

        chatModule.send(fPlayer, event);
    }
}
