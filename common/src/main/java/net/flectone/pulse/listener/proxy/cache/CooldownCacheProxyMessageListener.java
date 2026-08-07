package net.flectone.pulse.listener.proxy.cache;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.data.repository.CooldownRepository;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CooldownCacheProxyMessageListener implements PulseListener {

    private final CooldownRepository cooldownRepository;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.name() != ModuleName.UPDATE_CACHE_COOLDOWN) return event;
        if (event.sentByThisServer()) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            UUID uuid = UUID.fromString(proxyPayload.readString());
            String cooldownClass = proxyPayload.readString();
            long newExpireTime = proxyPayload.readLong();

            cooldownRepository.updateCache(uuid, cooldownClass, newExpireTime);
        }

        return event.withProcessed(true);
    }

}
