package net.flectone.pulse.listener.proxy.cache;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.module.message.tab.playerlist.MinecraftPlayerlistnameModule;
import net.flectone.pulse.util.constant.ModuleName;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPlayerDisconnectedProxyMessageListener implements PulseListener {

    private final MinecraftPlayerlistnameModule playerlistnameModule;

    @Pulse
    public void onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.name() != ModuleName.PLAYER_DISCONNECTED) return;

        if (!event.sentByThisServer()) {
            playerlistnameModule.remove(event.sender().uuid());
        }
    }

}
