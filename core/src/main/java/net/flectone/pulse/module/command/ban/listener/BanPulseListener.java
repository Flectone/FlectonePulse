package net.flectone.pulse.module.command.ban.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.ModerationMetadata;
import net.flectone.pulse.model.event.player.PlayerPreLoginEvent;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.kyori.adventure.text.Component;

@Singleton
public class BanPulseListener implements PulseListener {

    private final BanModule banModule;
    private final ModerationService moderationService;
    private final FPlayerService fPlayerService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final MessagePipeline messagePipeline;
    private final PermissionChecker permissionChecker;

    @Inject
    public BanPulseListener(BanModule banModule,
                            ModerationService moderationService,
                            FPlayerService fPlayerService,
                            ModerationMessageFormatter moderationMessageFormatter,
                            MessagePipeline messagePipeline,
                            PermissionChecker permissionChecker) {
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

            fPlayerService.loadColors(fPlayer);

            Localization.Command.Ban localization = banModule.localization(fPlayer);
            String formatPlayer = moderationMessageFormatter.replacePlaceholders(localization.getPerson(), fPlayer, ban);

            Component reason = messagePipeline.builder(fModerator, fPlayer, formatPlayer).build();
            event.setKickReason(reason);

            if (banModule.config().isShowConnectionAttempts()) {
                banModule.sendMessage(ModerationMetadata.<Localization.Command.Ban>builder()
                        .sender(fPlayer)
                        .format((fReceiver, message) -> {
                            String format = message.getConnectionAttempt();
                            return moderationMessageFormatter.replacePlaceholders(format, fReceiver, ban);
                        })
                        .moderation(ban)
                        .range(Range.get(Range.Type.SERVER))
                        .filter(filter -> permissionChecker.check(filter, banModule.getPermission()))
                        .build()
                );
            }
        }
    }

}
