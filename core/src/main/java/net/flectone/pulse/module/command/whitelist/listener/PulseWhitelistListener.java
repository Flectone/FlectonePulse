package net.flectone.pulse.module.command.whitelist.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.ModerationMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerPreLoginEvent;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.whitelist.WhitelistModule;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseWhitelistListener implements PulseListener {

    private final WhitelistModule whitelistModule;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final PermissionChecker permissionChecker;
    private final ModuleController moduleController;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;

    @Pulse
    public Event onPlayerPreLoginEvent(PlayerPreLoginEvent event) {
        if (!whitelistModule.isTurnedOn()) return event;

        // get player whitelist
        FPlayer fPlayer = event.player();
        if (permissionChecker.check(fPlayer, whitelistModule.permission().bypass())) return event;
        if (whitelistModule.isWhitelisted(fPlayer)) return event;

        Optional<Moderation> currentModeration = moderationService.getValid(fPlayerService.getConsole(), Moderation.Type.WHITELIST);
        if (currentModeration.isEmpty()) return event;

        // get moderator
        Moderation maintenance = currentModeration.get();
        FPlayer fModerator = fPlayerService.getFPlayer(maintenance.moderator());

        // show player connection for moderators
        if (whitelistModule.config().showConnectionAttempts()) {
            messageDispatcher.dispatch(whitelistModule, ModerationMetadata.<Localization.Command.Whitelist>builder()
                    .base(EventMetadata.<Localization.Command.Whitelist>builder()
                            .sender(fPlayer)
                            .format(Localization.Command.Whitelist::connectionAttempt)
                            .range(Range.get(Range.Type.SERVER))
                            .filter(filter -> permissionChecker.check(filter, whitelistModule.permission()))
                            .build()
                    )
                    .build()
            );
        }

        // replace string moderation placeholders
        Localization.Command.Whitelist localization = whitelistModule.localization(fPlayer);
        String formatPlayer = moderationMessageFormatter.replacePlaceholders(localization.person(), fPlayer, maintenance);

        // build message
        Component reason = messagePipeline.build(MessageContext.builder()
                .sender(fModerator)
                .receiver(fPlayer)
                .message(formatPlayer)
                .tagResolver(messagePipeline.targetTag("moderator", fPlayer, fModerator))
                .build()
        );

        return event
                .withPlayer(fPlayer)
                .withAllowed(false)
                .withKickReason(reason);
    }

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        if (!moduleController.isEnable(whitelistModule)) return;
        if (!whitelistModule.config().autoAdd()) return;
        if (whitelistModule.isTurnedOn()) return;

        long time = whitelistModule.config().autoAddDuration() * TimeFormatter.MULTIPLIER;

        FPlayer fPlayer = event.player();
        List<Moderation> whitelist = moderationService.getValid(fPlayer, Moderation.Type.WHITELIST, 1, 0);
        if (whitelist.stream().noneMatch(moderation -> moderation.isPermanent() || moderation.getRemainingTime() > time)) {
            whitelistModule.add(fPlayerService.getConsole(), fPlayer, time, null);
        }
    }

}
