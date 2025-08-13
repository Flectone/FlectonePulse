package net.flectone.pulse.module.message.tab.footer.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.module.message.tab.footer.FooterModule;

@Singleton
public class FooterPulseListener implements PulseListener {

    private final FooterModule footerModule;

    @Inject
    public FooterPulseListener(FooterModule footerModule) {
        this.footerModule = footerModule;
    }

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.getPlayer();
        footerModule.send(fPlayer);
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        if (!event.isReload()) return;

        FPlayer fPlayer = event.getPlayer();
        footerModule.send(fPlayer);
    }

}
