package net.flectone.pulse.platform.formatter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.moderation.caps.CapsModule;
import net.flectone.pulse.module.message.format.moderation.flood.FloodModule;
import net.flectone.pulse.module.message.format.moderation.newbie.NewbieModule;
import net.flectone.pulse.module.message.format.moderation.swear.SwearModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.MuteChecker;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModerationMessageFormatter {

    private final FileFacade fileFacade;
    private final TimeFormatter timeFormatter;
    private final ModerationService moderationService;
    private final Provider<IntegrationModule> integrationModuleProvider;
    private final Provider<CapsModule> capsModuleProvider;
    private final Provider<FloodModule> floodModuleProvider;
    private final Provider<NewbieModule> newbieModuleProvider;
    private final Provider<SwearModule> swearModuleProvider;
    private final MessagePipeline messagePipeline;
    private final FPlayerService fPlayerService;
    private final SocialService socialService;

    public Optional<MessageContext> createMuteContext(FPlayer fPlayer, MuteChecker.Status status) {
        return switch (status) {
            case LOCAL -> {
                List<Moderation> mutes = moderationService.getValid(fPlayer, Moderation.Type.MUTE, 1, 0);
                if (mutes.isEmpty()) yield Optional.empty();

                Moderation mute = mutes.getFirst();
                String format = fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().mute().person();

                MessageContext muteContext = MessageContext.builder()
                        .sender(fPlayer)
                        .message(replacePlaceholders(format, fPlayer, mute))
                        .tagResolver(messagePipeline.targetTag("moderator", fPlayer, fPlayerService.getFPlayer(mute.moderator())))
                        .build();

                yield Optional.of(muteContext);
            }
            case EXTERNAL -> {
                ExternalModeration mute = integrationModuleProvider.get().getMute(fPlayer);
                if (mute == null) yield Optional.empty();

                String format = fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().mute().person();

                MessageContext muteContext = MessageContext.builder()
                        .sender(fPlayer)
                        .message(replacePlaceholders(format, fPlayer, mute))
                        .tagResolver(messagePipeline.targetTag("moderator", fPlayer, fPlayerService.getFPlayer(mute.moderatorName())))
                        .build();

                yield Optional.of(muteContext);
            }
            case CAPS -> {
                CapsModule capsModule = capsModuleProvider.get();
                Long timestamp = moderationService.getFirstViolationTimestamp(fPlayer.uuid(), capsModule);
                if (timestamp == null) yield Optional.empty();

                String format = capsModule.localization(fPlayer).formatRestrict();

                MessageContext muteContext = MessageContext.builder()
                        .sender(fPlayer)
                        .message(replacePlaceholders(format, fPlayer, timestamp))
                        .tagResolver(messagePipeline.targetTag("moderator", fPlayer, fPlayerService.getConsole()))
                        .build();

                yield Optional.of(muteContext);
            }
            case FLOOD -> {
                FloodModule floodModule = floodModuleProvider.get();
                Long timestamp = moderationService.getFirstViolationTimestamp(fPlayer.uuid(), floodModule);
                if (timestamp == null) yield Optional.empty();

                String format = floodModule.localization(fPlayer).formatRestrict();

                MessageContext muteContext = MessageContext.builder()
                        .sender(fPlayer)
                        .message(replacePlaceholders(format, fPlayer, timestamp))
                        .tagResolver(messagePipeline.targetTag("moderator", fPlayer, fPlayerService.getConsole()))
                        .build();

                yield Optional.of(muteContext);
            }
            case NEWBIE -> {
                NewbieModule newbieModule = newbieModuleProvider.get();

                ExternalModeration mute = newbieModule.getModeration(fPlayer);
                if (mute == null) yield Optional.empty();

                String format = newbieModule.localization(fPlayer).formatRestrict();

                MessageContext muteContext = MessageContext.builder()
                        .sender(fPlayer)
                        .message(replacePlaceholders(format, fPlayer, mute))
                        .tagResolver(messagePipeline.targetTag("moderator", fPlayer, fPlayerService.getConsole()))
                        .build();

                yield Optional.of(muteContext);
            }
            case SWEAR -> {
                SwearModule swearModule = swearModuleProvider.get();
                Long timestamp = moderationService.getFirstViolationTimestamp(fPlayer.uuid(), swearModule);
                if (timestamp == null) yield Optional.empty();

                String format = swearModule.localization(fPlayer).formatRestrict();

                MessageContext muteContext = MessageContext.builder()
                        .sender(fPlayer)
                        .message(replacePlaceholders(format, fPlayer, timestamp))
                        .tagResolver(messagePipeline.targetTag("moderator", fPlayer, fPlayerService.getConsole()))
                        .build();

                yield Optional.of(muteContext);
            }
            default -> Optional.empty();
        };
    }

    public String replacePlaceholders(String message, FPlayer fReceiver, Moderation moderation) {
        Localization localization = fileFacade.localization(socialService.getSetting(fReceiver, SettingText.LOCALE));

        Localization.ReasonMap constantReasons = switch (moderation.type()) {
            case BAN -> localization.command().ban().reasons();
            case UNBAN -> localization.command().unban().reasons();
            case MUTE -> localization.command().mute().reasons();
            case UNMUTE -> localization.command().unmute().reasons();
            case WARN -> localization.command().warn().reasons();
            case UNWARN -> localization.command().unwarn().reasons();
            case KICK -> localization.command().kick().reasons();
            case WHITELIST, UNWHITELIST -> localization.command().whitelist().reasons();
            case MAINTENANCE, UNMAINTENANCE -> localization.command().maintenance().reasons();
        };

        String reason = constantReasons.getConstant(moderation.reason());
        return replacePlaceholders(message, fReceiver, moderation.id(), moderation.date(), moderation.time(), reason, moderation.isPermanent());
    }

    public String replacePlaceholders(String message, FPlayer fReceiver, ExternalModeration moderation) {
        return replacePlaceholders(message, fReceiver, moderation.moderationId(), moderation.date(), moderation.time(), moderation.reason(), moderation.permanent());
    }

    public String replacePlaceholders(String message, FPlayer fReceiver, Long timestamp) {
        return replacePlaceholders(message, fReceiver, -1, System.currentTimeMillis(), timestamp, "", false);
    }

    public String replacePlaceholders(String message, FPlayer fReceiver, long moderationId, long date, long time, String reason, boolean permanent) {
        Localization localization = fileFacade.localization(socialService.getSetting(fReceiver, SettingText.LOCALE));

        String formatDate = timeFormatter.formatDate(date);
        String formatTime = permanent
                ? localization.time().permanent()
                : timeFormatter.format(fReceiver, (Math.abs(date - time) + 500) / 1000 * 1000);
        String formatTimeLeft = permanent
                ? localization.time().permanent()
                : timeFormatter.format(fReceiver, time - System.currentTimeMillis());

        return replacePlaceholders(message, String.valueOf(moderationId), reason, formatDate, formatTime, formatTimeLeft);
    }

    public String replacePlaceholders(String message, String moderationId, String reason, String date, String time, String timeLeft) {
        return StringUtils.replaceEach(message,
                new String[]{"<id>", "<reason>", "<date>", "<time>", "<time_left>"},
                new String[]{moderationId, reason, date, time, timeLeft}
        );
    }
}