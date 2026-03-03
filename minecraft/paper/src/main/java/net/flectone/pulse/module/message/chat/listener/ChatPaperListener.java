package net.flectone.pulse.module.message.chat.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.function.BiConsumer;

public class ChatPaperListener implements Listener {

    private final FPlayerService fPlayerService;
    private final ChatModule chatModule;
    private final ModuleController moduleController;
    private final PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

    public ChatPaperListener(FPlayerService fPlayerService,
                             ModuleController moduleController,
                             ChatModule chatModule) {
        this.fPlayerService = fPlayerService;
        this.chatModule = chatModule;
        this.moduleController = moduleController;
    }

    @EventHandler
    public void asyncPlayerChatEvent(AsyncChatEvent event) {
        if (event.isCancelled()) return;
        if (!moduleController.isEnable(chatModule)) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());
        if (moduleController.isDisabledFor(chatModule, fPlayer)) return;

        String format = plainSerializer.serialize(event.message());

        Runnable cancelRunnable = () -> {
            event.setCancelled(true);
            event.viewers().clear();
        };

        BiConsumer<String, Boolean> successConsumer = (finalMessage, isCancel) -> {
            event.message(Component.text(finalMessage));
            event.setCancelled(isCancel);
            event.viewers().clear();
        };

        chatModule.handleChatEvent(fPlayer, format, cancelRunnable, successConsumer);
    }
}
