package net.flectone.pulse.module.message.nameplate.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.module.message.nameplate.NameplateModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NameplatePulseListener implements PulseListener {

    private final NameplateModule nameplateModule;

    @Pulse
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        nameplateModule.remove(event.player().uuid());
    }

}
