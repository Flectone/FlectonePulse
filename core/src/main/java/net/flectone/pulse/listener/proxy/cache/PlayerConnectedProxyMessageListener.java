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
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.PlaytimeService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerConnectedProxyMessageListener implements PulseListener {

    private final FPlayerService fPlayerService;
    private final PlaytimeService playtimeService;
    private final SocialService socialService;
    private final JoinModule joinModule;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) {
        if (event.name() != ModuleName.PLAYER_CONNECTED) return event;

        if (event.sentByThisServer()) {
            joinModule.send((FPlayer) event.sender(), false, false);
        } else {
            FEntity fEntity = event.sender();
            fPlayerService.invalidate(fEntity.uuid());
            playtimeService.invalidate(fEntity.uuid());
            socialService.invalidate(fEntity.uuid());
        }

        return event.withProcessed(true);
    }

}
