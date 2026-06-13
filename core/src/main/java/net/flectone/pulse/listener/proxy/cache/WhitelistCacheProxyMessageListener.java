package net.flectone.pulse.listener.proxy.cache;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.command.whitelist.WhitelistModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WhitelistCacheProxyMessageListener implements PulseListener {

    private final WhitelistModule whitelistModule;
    private final ModerationService moderationService;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final Gson gson;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.name() != ModuleName.UPDATE_CACHE_WHITELIST) return event;
        if (event.sentByThisServer()) return event.withProcessed(true);
        if (whitelistModule.config().filterByServer()) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            moderationService.invalidate(event.sender().uuid(), Moderation.Type.WHITELIST);
            moderationService.invalidate(event.sender().uuid(), Moderation.Type.UNWHITELIST);

            Moderation moderation = gson.fromJson(proxyPayload.readString(), Moderation.class);
            FPlayer fTarget = fPlayerService.getFPlayer(moderation.player());

            if (fTarget.isConsole()) {
                if (moderation.type() == Moderation.Type.WHITELIST) {
                    taskScheduler.runAsyncLater(() -> whitelistModule.kickOnlinePlayers(moderation));
                }

            } else if (moderation.type() == Moderation.Type.UNWHITELIST && whitelistModule.isTurnedOn()) {
                FPlayer fModerator = fPlayerService.getFPlayer(moderation.moderator());
                taskScheduler.runAsyncLater(() -> whitelistModule.kickPlayer(fModerator, fTarget));
            }
        }

        return event.withProcessed(true);
    }

}
