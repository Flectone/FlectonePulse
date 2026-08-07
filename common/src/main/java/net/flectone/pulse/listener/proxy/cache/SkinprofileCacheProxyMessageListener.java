package net.flectone.pulse.listener.proxy.cache;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.converter.ImagePixelConverter;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.service.SkinService;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SkinprofileCacheProxyMessageListener implements PulseListener {

    private final SkinService skinService;
    private final ImagePixelConverter imagePixelConverter;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.name() != ModuleName.UPDATE_CACHE_SKINPROFILE) return event;
        if (event.sentByThisServer()) return event;
        if (!(event.sender() instanceof FPlayer fPlayer)) return event;

        imagePixelConverter.invalidate(skinService.getSkin(fPlayer));
        imagePixelConverter.invalidate(skinService.getBodyUrl(fPlayer));
        imagePixelConverter.invalidate(skinService.getAvatarUrl(fPlayer));

        return event.withProcessed(true);
    }

}
