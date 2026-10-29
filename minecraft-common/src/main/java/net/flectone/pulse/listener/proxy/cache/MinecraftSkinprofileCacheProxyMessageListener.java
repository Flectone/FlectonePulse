package net.flectone.pulse.listener.proxy.cache;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.service.MinecraftSkinService;
import net.flectone.pulse.util.payload.ProxyPayload;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftSkinprofileCacheProxyMessageListener implements PulseListener {

    private final MinecraftSkinService minecraftSkinService;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.name() != ModuleName.UPDATE_CACHE_SKINPROFILE) return event;
        if (event.sentByThisServer()) return event.withProcessed(true);
        if (!(event.sender() instanceof FPlayer fPlayer)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = new ProxyPayload(event.payload())) {
            String name = proxyPayload.readString();
            String value = proxyPayload.readString();
            String signature = proxyPayload.readString();

            PlayerHeadObjectContents.ProfileProperty profileProperty = PlayerHeadObjectContents.property(name, value, signature.isEmpty() ? null : signature);
            minecraftSkinService.updateProfileProperty(fPlayer, profileProperty, true, false);
        }

        return event.withProcessed(true);
    }

}
