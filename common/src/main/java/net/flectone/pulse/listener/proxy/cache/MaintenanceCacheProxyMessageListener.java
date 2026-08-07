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
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MaintenanceCacheProxyMessageListener implements PulseListener {

    private final MaintenanceModule maintenanceModule;
    private final Gson gson;
    private final ModerationService moderationService;
    private final TaskScheduler taskScheduler;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.name() != ModuleName.UPDATE_CACHE_MAINTENANCE) return event;
        if (event.sentByThisServer()) return event.withProcessed(true);
        if (maintenanceModule.config().filterByServer()) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            moderationService.invalidate(event.sender().uuid(), Moderation.Type.MAINTENANCE);
            moderationService.invalidate(event.sender().uuid(), Moderation.Type.UNMAINTENANCE);

            Moderation moderation = gson.fromJson(proxyPayload.readString(), Moderation.class);
            if (moderation.type() == Moderation.Type.MAINTENANCE) {
                // give some time
                taskScheduler.runAsyncLater(() -> maintenanceModule.kickOnlinePlayers(moderation));
            }
        }

        return event.withProcessed(true);
    }

}
