package net.flectone.pulse.module.command.ban.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.message.context.ModerationMessageContext;
import net.flectone.pulse.model.event.player.PlayerPreLoginEvent;
import net.flectone.pulse.model.value.Moderation;
import net.flectone.pulse.model.value.Range;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseBanListener implements PulseListener {

    private final BanModule banModule;
    private final ModerationService moderationService;
    private final FPlayerService fPlayerService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final PermissionChecker permissionChecker;
    private final ModuleController moduleController;

    @Pulse
    public Event onPlayerPreLoginEvent(PlayerPreLoginEvent event) {
        // check module state
        if (!moduleController.isEnable(banModule)) return event;

        // get player bans
        FPlayer fPlayer = event.player();
        List<Moderation> bans = moderationService.getValid(fPlayer, Moderation.Type.BAN, 1, 0);
        if (bans.isEmpty()) return event;

        // get moderator
        Moderation ban = bans.getFirst();
        FPlayer fModerator = fPlayerService.getFPlayer(ban.moderator());

        // show player connection for moderators
        if (banModule.config().showConnectionAttempts()) {
            messageDispatcher.dispatch(banModule, EventMetadata.builder()
                    .range(Range.get(Range.Type.SERVER))
                    .filter(filter -> permissionChecker.check(filter, banModule.permission()))
                    .messageContext(fResolver -> ModerationMessageContext.builder()
                            .base(MessageContext.builder()
                                    .sender(fPlayer)
                                    .receiver(fResolver)
                                    .message(moderationMessageFormatter.replacePlaceholders(banModule.localization(fResolver).connectionAttempt(), fResolver, ban))
                                    .tagResolver(messagePipeline.targetTag("moderator", fResolver, fModerator))
                                    .build()
                            )
                            .moderation(ban)
                            .build()
                    )
                    .build()
            );
        }

        return event
                .withPlayer(fPlayer)
                .withAllowed(false)
                .withKickReason(messagePipeline.build(ModerationMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fModerator)
                                .receiver(fPlayer)
                                .message(moderationMessageFormatter.replacePlaceholders(banModule.localization(fPlayer).person(), fPlayer, ban))
                                .tagResolver(messagePipeline.targetTag("moderator", fPlayer, fModerator))
                                .build()
                        )
                        .moderation(ban)
                        .build()
                ));
    }

}
