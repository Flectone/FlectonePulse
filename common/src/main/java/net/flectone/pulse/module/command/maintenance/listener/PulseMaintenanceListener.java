package net.flectone.pulse.module.command.maintenance.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.message.context.ModerationMessageContext;
import net.flectone.pulse.model.event.player.PlayerPreLoginEvent;
import net.flectone.pulse.model.value.Moderation;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseMaintenanceListener implements PulseListener {

    private final MaintenanceModule maintenanceModule;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;

    @Pulse
    public PlayerPreLoginEvent onPlayerPreLoginEvent(PlayerPreLoginEvent event) {
        FPlayer fPlayer = event.player();
        if (maintenanceModule.isAllowed(fPlayer)) return event;

        Optional<Moderation> currentModeration = moderationService.getValid(fPlayerService.getConsole(), Moderation.Type.MAINTENANCE);
        if (currentModeration.isEmpty()) return event;

        // get moderator
        Moderation maintenance = currentModeration.get();
        FPlayer fModerator = fPlayerService.getFPlayer(maintenance.moderator());

        return event
                .withPlayer(fPlayer)
                .withAllowed(false)
                .withKickReason(messagePipeline.build(ModerationMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fModerator)
                                .receiver(fPlayer)
                                .message(moderationMessageFormatter.replacePlaceholders(maintenanceModule.localization(fPlayer).person(), fPlayer, maintenance))
                                .tagResolver(messagePipeline.targetTag("moderator", fPlayer, fModerator))
                                .build()
                        )
                        .moderation(maintenance)
                        .build()
                ));
    }

}
