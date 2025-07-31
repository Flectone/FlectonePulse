package net.flectone.pulse.module.message.brand.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.module.message.brand.BrandModule;

@Singleton
public class BrandPulseListener implements PulseListener {

    private final BrandModule brandModule;

    @Inject
    public BrandPulseListener(BrandModule brandModule) {
        this.brandModule = brandModule;
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        brandModule.send(event.getPlayer());
    }

}
