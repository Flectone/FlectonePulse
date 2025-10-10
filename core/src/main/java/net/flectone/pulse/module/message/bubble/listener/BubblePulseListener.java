package net.flectone.pulse.module.message.bubble.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.module.message.bubble.service.BubbleService;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BubblePulseListener implements PulseListener {

    private final BubbleService bubbleService;

    @Pulse
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        FPlayer fPlayer = event.getPlayer();
        bubbleService.clear(fPlayer);
    }

}
