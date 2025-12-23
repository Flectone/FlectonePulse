package net.flectone.pulse.module.message.update.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.module.message.update.UpdateModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UpdatePulseListener implements PulseListener {

    private final UpdateModule updateModule;

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.player();
        updateModule.send(fPlayer);
    }

}
