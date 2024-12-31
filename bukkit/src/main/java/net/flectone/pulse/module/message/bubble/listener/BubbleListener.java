package net.flectone.pulse.module.message.bubble.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.module.message.bubble.BukkitBubbleModule;
import net.flectone.pulse.module.message.chat.BukkitChatModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@Singleton
public class BubbleListener implements Listener {

    private final FPlayerManager fPlayerManager;
    private final BukkitBubbleModule bubbleModule;
    private final BukkitChatModule chatModule;

    @Inject
    public BubbleListener(FPlayerManager fPlayerManager,
                          BukkitBubbleModule bubbleModule,
                          BukkitChatModule chatModule) {
        this.fPlayerManager = fPlayerManager;
        this.bubbleModule = bubbleModule;
        this.chatModule = chatModule;
    }

    @EventHandler
    public void asyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        if (event.isCancelled() && !event.getRecipients().isEmpty()) return;
        if (chatModule.isEnable()) return;

        bubbleModule.add(fPlayerManager.get(event.getPlayer()), event.getMessage());
    }
}
