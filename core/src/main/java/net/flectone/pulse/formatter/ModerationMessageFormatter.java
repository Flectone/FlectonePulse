package net.flectone.pulse.formatter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.MuteChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.ExternalModeration;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.moderation.newbie.NewbieModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;

import java.util.List;

@Singleton
public class ModerationMessageFormatter {

    private final FileManager fileManager;
    private final FPlayerService fPlayerService;
    private final TimeFormatter timeFormatter;
    private final ModerationService moderationService;
    private final IntegrationModule integrationModule;

    @Inject private NewbieModule newbieModule;

    @Inject
    public ModerationMessageFormatter(FileManager fileManager,
                                      FPlayerService fPlayerService,
                                      TimeFormatter timeFormatter,
                                      ModerationService moderationService,
                                      IntegrationModule integrationModule) {
        this.fileManager = fileManager;
        this.fPlayerService = fPlayerService;
        this.timeFormatter = timeFormatter;
        this.moderationService = moderationService;
        this.integrationModule = integrationModule;
    }

    public String replacePlaceholders(String message,
                                      String playerName,
                                      String moderatorName,
                                      String moderationId,
                                      String reason,
                                      String date,
                                      String time,
                                      String timeLeft) {
        return message
                .replace("<player>", playerName)
                .replace("<moderator>", moderatorName)
                .replace("<id>", moderationId)
                .replace("<reason>", reason)
                .replace("<date>", date)
                .replace("<time>", time)
                .replace("<time_left>", timeLeft);
    }

    public String replacePlaceholders(String message, FPlayer fReceiver, Moderation moderation) {
        Localization localization = fileManager.getLocalization(fReceiver);

        Localization.ReasonMap constantReasons = switch (moderation.getType()) {
            case BAN -> localization.getCommand().getBan().getReasons();
            case MUTE -> localization.getCommand().getMute().getReasons();
            case WARN -> localization.getCommand().getWarn().getReasons();
            case KICK -> localization.getCommand().getKick().getReasons();
        };

        FPlayer fTarget = fPlayerService.getFPlayer(moderation.getPlayer());
        FPlayer fModerator = fPlayerService.getFPlayer(moderation.getModerator());
        String reason = constantReasons.getConstant(moderation.getReason());
        String date = timeFormatter.formatDate(moderation.getDate());
        String time = moderation.isPermanent()
                ? localization.getTime().getPermanent()
                : timeFormatter.format(fReceiver, moderation.getOriginalTime());
        String timeLeft = moderation.isPermanent()
                ? localization.getTime().getPermanent()
                : timeFormatter.format(fReceiver, moderation.getRemainingTime());

        return replacePlaceholders(message, fTarget.getName(), fModerator.getName(),
                String.valueOf(moderation.getId()), reason, date, time, timeLeft
        );
    }

    public String replacePlaceholders(String message, FPlayer fReceiver, ExternalModeration moderation) {
        Localization localization = fileManager.getLocalization(fReceiver);

        String date = timeFormatter.formatDate(moderation.date());
        String time = moderation.permanent()
                ? localization.getTime().getPermanent()
                : timeFormatter.format(fReceiver, moderation.time());
        String timeLeft = moderation.permanent()
                ? localization.getTime().getPermanent()
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
        String format = fileManager.getLocalization(fPlayer).getCommand().getMute().getPerson();

        return switch (status) {
            case LOCAL -> {
                List<Moderation> mutes = moderationService.getValidMutes(fPlayer);
                if (mutes.isEmpty()) yield format;

                yield replacePlaceholders(format, fPlayer, mutes.get(0));
            }
            case EXTERNAL -> {
                ExternalModeration mute = integrationModule.getMute(fPlayer);
                if (mute == null) yield format;

                yield replacePlaceholders(format, fPlayer, mute);
            }
            case NEWBIE -> {
                ExternalModeration mute = newbieModule.getModeration(fPlayer);
                if (mute == null) yield format;

                yield replacePlaceholders(format, fPlayer, mute);
            }
            default -> "";
        };
    }
}