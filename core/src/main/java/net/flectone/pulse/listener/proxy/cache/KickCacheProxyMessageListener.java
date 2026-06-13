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
import net.flectone.pulse.module.command.kick.KickModule;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class KickCacheProxyMessageListener implements PulseListener {

    private final KickModule kickModule;
    private final Gson gson;
    private final TaskScheduler taskScheduler;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.name() != ModuleName.UPDATE_CACHE_KICK) return event;
        if (event.sentByThisServer()) return event.withProcessed(true);
        if (kickModule.config().filterByServer()) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            Moderation moderation = gson.fromJson(proxyPayload.readString(), Moderation.class);

            // give some time
            taskScheduler.runAsyncLater(() -> kickModule.kick(moderation));
        }

        return event.withProcessed(true);
    }

}
