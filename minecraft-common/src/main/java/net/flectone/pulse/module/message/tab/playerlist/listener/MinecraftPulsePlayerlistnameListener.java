package net.flectone.pulse.module.message.tab.playerlist.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.event.player.PlayerPersistAndDisposeEvent;
import net.flectone.pulse.module.message.tab.playerlist.MinecraftPlayerlistnameModule;
import net.flectone.pulse.util.constant.ModuleName;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPulsePlayerlistnameListener implements PulseListener {

    private final MinecraftPlayerlistnameModule playerlistnameModule;

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        playerlistnameModule.update();
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        if (!event.reload()) return;

        playerlistnameModule.update();
    }

    @Pulse
    public void onPlayerPersistAndDisposeEvent(PlayerPersistAndDisposeEvent event) {
        playerlistnameModule.clearProxyPlayers(event.player().uuid());
    }

    @Pulse
    public void onProxyMessageEvent(ProxyMessageEvent event) {
        if (event.name() == ModuleName.PLAYER_DISCONNECTED || event.name() == ModuleName.PLAYER_CONNECTED) {
            if (!event.sentByThisServer()) {
                // remove from proxy cache
                playerlistnameModule.remove(event.sender().uuid());

                // do not wait for ticker, update it
                playerlistnameModule.update();
            }
        }
    }

}
