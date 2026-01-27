package net.flectone.pulse.module.message.chat.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.listener.HytaleListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.chat.HytaleChatModule;
import net.flectone.pulse.service.FPlayerService;

import java.util.function.BiConsumer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ChatHytaleListener implements HytaleListener {

    private final FPlayerService fPlayerService;
    private final HytaleChatModule chatModule;

    public void onPlayerChatEvent(PlayerChatEvent event) {
        if (event.isCancelled()) return;
        if (event.getTargets().isEmpty()) return;
        if (!chatModule.isEnable()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getSender().getUuid());
        if (chatModule.isModuleDisabledFor(fPlayer)) return;

        Runnable cancelRunnable = () -> {
            event.setCancelled(true);
            event.getTargets().clear();
        };

        BiConsumer<String, Boolean> successConsumer = (finalMessage, isCancel) -> {
            event.setContent(finalMessage);
            event.setCancelled(isCancel);
            event.getTargets().clear();
        };

        chatModule.handleChatEvent(fPlayer, event.getContent(), cancelRunnable, successConsumer);
    }

}
