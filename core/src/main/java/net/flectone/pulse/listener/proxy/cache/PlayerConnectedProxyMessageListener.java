package net.flectone.pulse.listener.proxy.cache;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.PlaytimeService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerConnectedProxyMessageListener implements PulseListener {

    private final FPlayerService fPlayerService;
    private final PlaytimeService playtimeService;
    private final SocialService socialService;
    private final JoinModule joinModule;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.name() != ModuleName.PLAYER_CONNECTED) return event;

        try (ProxyPayload proxyPayload = event.openPayload()) {
            boolean firstTime = proxyPayload.readBoolean();

            if (event.sentByThisServer()) {
                if (firstTime) {
                    joinModule.send((FPlayer) event.sender(), false, false);
                }
            } else {
                UUID playerUUID = event.sender().uuid();

                // offline -> online player
                fPlayerService.invalidateOfflineCache(playerUUID, true);

                // clear playtime cache
                playtimeService.invalidate(playerUUID);

                // clear social cache
                socialService.invalidate(playerUUID);
            }
        }


        return event.withProcessed(true);
    }

}
