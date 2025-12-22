package net.flectone.pulse.platform.formatter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.util.checker.MuteChecker;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.moderation.newbie.NewbieModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModerationMessageFormatter {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final TimeFormatter timeFormatter;
    private final ModerationService moderationService;
    private final Provider<IntegrationModule> integrationModuleProvider;
    private final Provider<NewbieModule> newbieModuleProvider;

    public String replacePlaceholders(String message,
                                      String playerName,
                                      String moderatorName,
                                      String moderationId,
                                      String reason,
                                      String date,
                                      String time,
                                      String timeLeft) {
        return StringUtils.replaceEach(message,
                new String[]{"<player>", "<moderator>", "<id>", "<reason>", "<date>", "<time>", "<time_left>"},
                new String[]{playerName, moderatorName, moderationId, reason, date, time, timeLeft}
        );
    }

    public String replacePlaceholders(String message, FPlayer fReceiver, Moderation moderation) {
        Localization localization = fileFacade.localization(fReceiver);

        Localization.ReasonMap constantReasons = switch (moderation.type()) {
            case BAN -> localization.command().ban().reasons();
            case MUTE -> localization.command().mute().reasons();
            case WARN -> localization.command().warn().reasons();
            case KICK -> localization.command().kick().reasons();
        };

        FPlayer fTarget = fPlayerService.getFPlayer(moderation.player());
        FPlayer fModerator = fPlayerService.getFPlayer(moderation.moderator());
        String reason = constantReasons.getConstant(moderation.reason());
        String date = timeFormatter.formatDate(moderation.date());
        String time = moderation.isPermanent()
                ? localization.time().permanent()
                : timeFormatter.format(fReceiver, moderation.getOriginalTime());
        String timeLeft = moderation.isPermanent()
                ? localization.time().permanent()
                : timeFormatter.format(fReceiver, moderation.getRemainingTime());

        return replacePlaceholders(message, fTarget.getName(), fModerator.getName(),
                String.valueOf(moderation.id()), reason, date, time, timeLeft
        );
    }

    public String replacePlaceholders(String message, FPlayer fReceiver, ExternalModeration moderation) {
        Localization localization = fileFacade.localization(fReceiver);

        String date = timeFormatter.formatDate(moderation.date());
        String time = moderation.permanent()
                ? localization.time().permanent()
                : timeFormatter.format(fReceiver, moderation.time());
        String timeLeft = moderation.permanent()
                ? localization.time().permanent()
                : timeFormatter.format(fReceiver, moderation.time() - System.currentTimeMillis());

        return replacePlaceholders(message,
                moderation.playerName(),
                moderation.moderatorName(),
                String.valueOf(moderation.moderationId()),
                moderation.reason(),
                date,
                time,
                timeLeft
        );
    }

    public String buildMuteMessage(FPlayer fPlayer, MuteChecker.Status status) {
        String format = fileFacade.localization(fPlayer).command().mute().person();

        return switch (status) {
            case LOCAL -> {
                List<Moderation> mutes = moderationService.getValidMutes(fPlayer);
                if (mutes.isEmpty()) yield format;

                yield replacePlaceholders(format, fPlayer, mutes.getFirst());
            }
            case EXTERNAL -> {
                ExternalModeration mute = integrationModuleProvider.get().getMute(fPlayer);
                if (mute == null) yield format;

                yield replacePlaceholders(format, fPlayer, mute);
            }
            case NEWBIE -> {
                ExternalModeration mute = newbieModuleProvider.get().getModeration(fPlayer);
                if (mute == null) yield format;

                yield replacePlaceholders(format, fPlayer, mute);
            }
            default -> "";
        };
    }
}