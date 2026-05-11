package net.flectone.pulse.module.command.tell.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.module.command.tell.TellModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseTellListener implements PulseListener {

    private final TellModule tellModule;

    @Pulse
    public void onPlayerQuit(PlayerQuitEvent event) {
        tellModule.removeReceiver(event.player());
    }

}
