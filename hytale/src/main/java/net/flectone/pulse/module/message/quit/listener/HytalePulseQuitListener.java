package net.flectone.pulse.module.message.quit.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.module.message.quit.QuitModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytalePulseQuitListener implements PulseListener {

    private final QuitModule quitModule;

    @Pulse
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        FPlayer fPlayer = event.player();
        quitModule.send(fPlayer, false);
    }

}
