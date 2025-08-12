package net.flectone.pulse.module.message.chat.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.chat.BukkitChatModule;
import net.flectone.pulse.service.FPlayerService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.function.BiConsumer;

@Singleton
public class ChatBukkitListener implements Listener {

    private final FPlayerService fPlayerService;
    private final BukkitChatModule chatModule;

    @Inject
    public ChatBukkitListener(FPlayerService fPlayerService,
                              BukkitChatModule chatModule) {
        this.fPlayerService = fPlayerService;
        this.chatModule = chatModule;
    }

    @EventHandler
    public void asyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        if (event.getRecipients().isEmpty()) return;
        if (!event.getFormat().equals("<%1$s> %2$s")) return;
        if (!chatModule.isEnable()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer());
        if (chatModule.isModuleDisabledFor(fPlayer)) return;

        Runnable cancelRunnable = () -> {
            event.setCancelled(true);
            event.getRecipients().clear();
        };

        BiConsumer<String, Boolean> successConsumer = (finalMessage, isCancel) -> {
            event.setMessage(finalMessage);
            event.setCancelled(isCancel);
            event.getRecipients().clear();
        };

        chatModule.send(fPlayer, event.getMessage(), cancelRunnable, successConsumer);
    }
}
