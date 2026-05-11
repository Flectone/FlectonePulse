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
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.kyori.adventure.text.Component;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseWhitelistListener implements PulseListener {

    private final WhitelistModule whitelistModule;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final PermissionChecker permissionChecker;
    private final ModuleController moduleController;

    @Pulse
    public Event onPlayerPreLoginEvent(PlayerPreLoginEvent event) {
        // check module state
        if (!moduleController.isEnable(whitelistModule)) return event;
        if (!whitelistModule.config().turnedOn()) return event;

        // get player whitelist
        FPlayer fPlayer = event.player();
        if (permissionChecker.check(fPlayer, whitelistModule.permission().bypass())) return event;
        if (!whitelistModule.getWhitelist(fPlayer).isEmpty()) return event;

        // load custom player colors
        fPlayer = fPlayerService.loadColors(fPlayer);

        // build message
        MessageContext messageContext = messagePipeline.createContext(fPlayer, whitelistModule.localization(fPlayer).person());
        Component reason = messagePipeline.build(messageContext);

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

        return event.withPlayer(fPlayer).withAllowed(false).withKickReason(reason);
    }

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        if (!moduleController.isEnable(whitelistModule)) return;
        if (!whitelistModule.config().autoAdd()) return;
        if (whitelistModule.config().turnedOn()) return;

        long time = whitelistModule.config().autoAddDuration() * TimeFormatter.MULTIPLIER;

        FPlayer fPlayer = event.player();
        List<Moderation> whitelist = whitelistModule.getWhitelist(fPlayer);
        if (whitelist.stream().noneMatch(moderation -> moderation.isPermanent() || moderation.getRemainingTime() > time)) {
            whitelistModule.add(fPlayerService.getConsole(), fPlayer, time, null);
        }
    }

}
