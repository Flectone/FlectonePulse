package net.flectone.pulse.module.message.brand.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.module.message.brand.BrandModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BrandPulseListener implements PulseListener {

    private final BrandModule brandModule;

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        brandModule.send(event.player());
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        if (!event.reload()) return;

        brandModule.send(event.player());
    }

}
