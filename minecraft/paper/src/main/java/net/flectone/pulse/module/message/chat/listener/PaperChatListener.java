package net.flectone.pulse.module.message.chat.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.processing.PaperComponentSerializer;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.function.BiConsumer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PaperChatListener implements Listener {

    private final FPlayerService fPlayerService;
    private final ChatModule chatModule;
    private final ModuleController moduleController;
    private final PaperComponentSerializer paperComponentSerializer;

    @EventHandler
    public void onAsyncChatEvent(AsyncChatEvent event) {
        if (event.isCancelled()) return;
        if (!moduleController.isEnable(chatModule)) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());
        if (moduleController.isDisabledFor(chatModule, fPlayer)) return;

        String format = paperComponentSerializer.toPlain(event.message()).orElse("");

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
