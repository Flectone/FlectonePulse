package net.flectone.pulse.listener.proxy.cache;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BanCacheProxyMessageListener implements PulseListener {

    private final BanModule banModule;
    private final Gson gson;
    private final ModerationService moderationService;
    private final TaskScheduler taskScheduler;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.name() != ModuleName.UPDATE_CACHE_BAN) return event;
        if (event.sentByThisServer()) return event.withProcessed(true);
        if (banModule.config().filterByServer()) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            moderationService.invalidate(event.sender().uuid(), Moderation.Type.BAN);
            moderationService.invalidate(event.sender().uuid(), Moderation.Type.UNBAN);

            Moderation moderation = gson.fromJson(proxyPayload.readString(), Moderation.class);
            if (moderation.type() == Moderation.Type.BAN) {
                // give some time
                taskScheduler.runAsyncLater(() -> banModule.kick(moderation));
            }
        }

        return event.withProcessed(true);
    }

}
