package net.flectone.pulse.module.message.chat.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.function.BiConsumer;

public class ChatPaperListener implements Listener {

    private final FPlayerService fPlayerService;
    private final ChatModule chatModule;
    private final PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

    public ChatPaperListener(FPlayerService fPlayerService,
                             ChatModule chatModule) {
        this.fPlayerService = fPlayerService;
        this.chatModule = chatModule;
    }

    @EventHandler
    public void asyncPlayerChatEvent(AsyncChatEvent event) {
        if (event.isCancelled()) return;
        if (!chatModule.isEnable()) return;

        Runnable cancelRunnable = () -> {
            event.setCancelled(true);
            event.viewers().clear();
        };

        BiConsumer<String, Boolean> successConsumer = (finalMessage, isCancel) -> {
            event.message(Component.text(finalMessage));
            event.setCancelled(isCancel);
            event.viewers().clear();
        };

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer());
        String format = plainSerializer.serialize(event.message());

        chatModule.send(fPlayer, format, cancelRunnable, successConsumer);
    }
}
