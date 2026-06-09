package net.flectone.pulse.listener.proxy.cache;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ColorCacheProxyMessageListener implements PulseListener {

    private final SocialService socialService;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.name() != ModuleName.UPDATE_CACHE_COLOR) return event;
        if (event.sentByThisServer()) return event.withProcessed(true);

        if (event.sender() instanceof FPlayer fPlayer) {
            socialService.loadColors(fPlayer, false);
        }

        return event.withProcessed(true);
    }

}
