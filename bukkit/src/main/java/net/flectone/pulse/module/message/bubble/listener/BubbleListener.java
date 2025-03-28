package net.flectone.pulse.module.message.bubble.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.bubble.BukkitBubbleModule;
import net.flectone.pulse.module.message.chat.BukkitChatModule;
import net.flectone.pulse.service.FPlayerService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@Singleton
public class BubbleListener implements Listener {

    private final FPlayerService fPlayerService;
    private final BukkitBubbleModule bubbleModule;
    private final BukkitChatModule chatModule;

    @Inject
    public BubbleListener(FPlayerService fPlayerService,
                          BukkitBubbleModule bubbleModule,
                          BukkitChatModule chatModule) {
        this.fPlayerService = fPlayerService;
        this.bubbleModule = bubbleModule;
        this.chatModule = chatModule;
    }

    @EventHandler
    public void asyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        if (event.isCancelled() && !event.getRecipients().isEmpty()) return;
        if (chatModule.isEnable()) return;

        bubbleModule.add(fPlayerService.getFPlayer(event.getPlayer()), event.getMessage());
    }
}
