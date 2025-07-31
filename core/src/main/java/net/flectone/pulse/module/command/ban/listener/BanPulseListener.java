package net.flectone.pulse.module.command.ban.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.formatter.ModerationMessageFormatter;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.model.Range;
import net.flectone.pulse.model.event.player.PlayerPreLoginEvent;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.kyori.adventure.text.Component;

@Singleton
public class BanPulseListener implements PulseListener {

    private final Command.Ban command;
    private final BanModule banModule;
    private final ModerationService moderationService;
    private final FPlayerService fPlayerService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final MessagePipeline messagePipeline;
    private final PermissionChecker permissionChecker;

    @Inject
    public BanPulseListener(FileResolver fileResolver,
                            BanModule banModule,
                            ModerationService moderationService,
                            FPlayerService fPlayerService,
                            ModerationMessageFormatter moderationMessageFormatter,
                            MessagePipeline messagePipeline,
                            PermissionChecker permissionChecker) {
        this.command = fileResolver.getCommand().getBan();
        this.banModule = banModule;
        this.moderationService = moderationService;
        this.fPlayerService = fPlayerService;
        this.moderationMessageFormatter = moderationMessageFormatter;
        this.messagePipeline = messagePipeline;
        this.permissionChecker = permissionChecker;
    }

    @Pulse
    public void onPlayerPreLoginEvent(PlayerPreLoginEvent event) {
        if (!banModule.isEnable()) return;

        FPlayer fPlayer = event.getPlayer();
        for (Moderation ban : moderationService.getValidBans(fPlayer)) {
            event.setAllowed(false);

            FPlayer fModerator = fPlayerService.getFPlayer(ban.getModerator());

            fPlayerService.loadSettings(fPlayer);
            fPlayerService.loadColors(fPlayer);

            Localization.Command.Ban localization = banModule.resolveLocalization(fPlayer);
            String formatPlayer = moderationMessageFormatter.replacePlaceholders(localization.getPerson(), fPlayer, ban);

            Component reason = messagePipeline.builder(fModerator, fPlayer, formatPlayer).build();
            event.setKickReason(reason);

            if (command.isShowConnectionAttempts()) {
                banModule.builder(fPlayer)
                        .range(Range.get(Range.Type.SERVER))
                        .filter(filter -> permissionChecker.check(filter, banModule.getModulePermission()))
                        .format((fReceiver, message) -> {
                            String format = message.getConnectionAttempt();
                            return moderationMessageFormatter.replacePlaceholders(format, fReceiver, ban);
                        })
                        .sendBuilt();
            }
        }
    }

}
