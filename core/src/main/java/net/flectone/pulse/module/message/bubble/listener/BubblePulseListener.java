package net.flectone.pulse.module.message.bubble.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.module.message.bubble.service.BubbleService;

@Singleton
public class BubblePulseListener implements PulseListener {

    private final BubbleService bubbleService;

    @Inject
    public BubblePulseListener(BubbleService bubbleService) {
        this.bubbleService = bubbleService;
    }

    @Pulse
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        FPlayer fPlayer = event.getPlayer();
        bubbleService.clear(fPlayer);
    }

}
