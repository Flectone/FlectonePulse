package net.flectone.pulse.listener.proxy.cache;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.ModuleName;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MuteCacheProxyMessageListener implements PulseListener {

    private final MuteModule muteModule;
    private final ModerationService moderationService;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.name() != ModuleName.UPDATE_CACHE_MUTE) return event;
        if (event.sentByThisServer()) return event.withProcessed(true);
        if (muteModule.config().filterByServer()) return event.withProcessed(true);

        moderationService.invalidate(event.sender().uuid(), Moderation.Type.MUTE);
        moderationService.invalidate(event.sender().uuid(), Moderation.Type.UNMUTE);

        return event.withProcessed(true);
    }

}
