package net.flectone.pulse.listener.proxy.cache;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.service.MinecraftSkinService;
import net.flectone.pulse.util.constant.ModuleName;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftSkinprofileCacheProxyMessageListener implements PulseListener {

    private final MinecraftSkinService minecraftSkinService;

    @Pulse
    public void onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.name() != ModuleName.UPDATE_CACHE_SKINPROFILE) return;
        if (event.sentByThisServer()) return;
        if (!(event.sender() instanceof FPlayer fPlayer)) return;

        minecraftSkinService.updateProfilePropertyCache(fPlayer);
    }

}
