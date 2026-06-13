package net.flectone.pulse.listener.proxy.cache;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ViolationCacheProxyMessageListener implements PulseListener {

    private final ModerationService moderationService;
    private final Gson gson;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.name() != ModuleName.UPDATE_CACHE_VIOLATION) return event;
        if (event.sentByThisServer()) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            ModerationService.ViolationKey violationKey = gson.fromJson(proxyPayload.readString(), ModerationService.ViolationKey.class);
            long violationValue = proxyPayload.readLong();

            moderationService.addViolation(violationKey, violationValue);
        }

        return event.withProcessed(true);
    }

}
