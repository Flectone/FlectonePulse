package net.flectone.pulse.module.message.join.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.module.message.join.JoinModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class JoinPulseListener implements PulseListener {

    private final JoinModule joinModule;

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.player();
        joinModule.send(fPlayer, false);
    }

}
