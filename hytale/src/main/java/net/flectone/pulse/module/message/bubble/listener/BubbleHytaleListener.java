package net.flectone.pulse.module.message.bubble.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.listener.HytaleListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.service.FPlayerService;

import java.util.Collections;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BubbleHytaleListener implements HytaleListener {

    private final FPlayerService fPlayerService;
    private final BubbleModule bubbleModule;
    private final ChatModule chatModule;

    public void onPlayerChatEvent(PlayerChatEvent event) {
        if (chatModule.isEnable() || !bubbleModule.isEnable()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getSender().getUuid());

        String message = event.getContent();

        bubbleModule.add(fPlayer, message, Collections.emptyList());
    }
}
