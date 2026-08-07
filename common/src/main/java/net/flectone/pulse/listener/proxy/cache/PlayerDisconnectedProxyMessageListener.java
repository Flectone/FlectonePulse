package net.flectone.pulse.listener.proxy.cache;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.PlaytimeService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;

import java.io.IOException;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerDisconnectedProxyMessageListener implements PulseListener {

    private final FPlayerService fPlayerService;
    private final PlaytimeService playtimeService;
    private final SocialService socialService;
    private final QuitModule quitModule;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.name() != ModuleName.PLAYER_DISCONNECTED) return event;

        FEntity fEntity = event.sender();
        if (fEntity instanceof FPlayer fPlayer) {
            quitModule.send(fPlayer, false, false);
        }

        if (!event.sentByThisServer()) {
            UUID playerUUID = fEntity.uuid();

            // online -> offline player
            fPlayerService.invalidateOnlineCache(playerUUID);

            // clear playtime cache
            playtimeService.invalidate(playerUUID);

            // clear social cache
            socialService.invalidate(playerUUID);
        }

        return event.withProcessed(true);
    }

}
