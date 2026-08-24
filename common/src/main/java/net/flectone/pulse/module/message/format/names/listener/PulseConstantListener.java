package net.flectone.pulse.module.message.format.names.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.module.message.format.names.NamesModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseConstantListener implements PulseListener {

    private final NamesModule namesModule;

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.player();
        namesModule.updateConstant(fPlayer);
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        if (!event.reload()) return;

        FPlayer fPlayer = event.player();
        namesModule.updateConstant(fPlayer);
    }

}
