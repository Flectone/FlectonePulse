package net.flectone.pulse.platform.formatter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.checker.MuteChecker;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.ExternalModerationMessageContext;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.message.context.ModerationMessageContext;
import net.flectone.pulse.model.value.ExternalModeration;
import net.flectone.pulse.model.value.Moderation;
import net.flectone.pulse.module.message.format.moderation.caps.CapsModule;
import net.flectone.pulse.module.message.format.moderation.flood.FloodModule;
import net.flectone.pulse.module.message.format.moderation.newbie.NewbieModule;
import net.flectone.pulse.module.message.format.moderation.swear.SwearModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.service.ExternalMuteService;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.LazyInstance;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModerationMessageFormatterImpl implements ModerationMessageFormatter {

    private final FileFacade fileFacade;
    private final TimeFormatter timeFormatter;
    private final ModerationService moderationService;
    private final ExternalMuteService externalMuteService;
    private final LazyInstance<CapsModule> capsModule;
    private final LazyInstance<FloodModule> floodModule;
    private final LazyInstance<NewbieModule> newbieModule;
    private final LazyInstance<SwearModule> swearModule;
    private final MessagePipeline messagePipeline;
    private final FPlayerService fPlayerService;
    private final SocialService socialService;

    @Override
    public Optional<MessageContext> createMuteContext(FPlayer fPlayer, MuteChecker.Status status) {
        return switch (status) {
            case LOCAL -> {
                List<Moderation> mutes = moderationService.getValid(fPlayer, Moderation.Type.MUTE, 1, 0);
                if (mutes.isEmpty()) yield Optional.empty();

                Moderation mute = mutes.getFirst();
                String format = fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().mute().person();

                MessageContext muteContext = ModerationMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fPlayer)
                                .message(replacePlaceholders(format, fPlayer, mute))
                                .tagResolver(messagePipeline.targetTag("moderator", fPlayer, fPlayerService.getFPlayer(mute.moderator())))
                                .build()
                        )
                        .moderation(mute)
                        .build();

                yield Optional.of(muteContext);
            }
            case EXTERNAL -> {
                Optional<ExternalModeration> optionalMute = externalMuteService.get(fPlayer);
                if (optionalMute.isEmpty()) yield Optional.empty();

                ExternalModeration mute = optionalMute.get();

                String format = fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().mute().person();

                MessageContext muteContext = ExternalModerationMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fPlayer)
                                .message(replacePlaceholders(format, fPlayer, mute))
                                .tagResolver(messagePipeline.targetTag("moderator", fPlayer, fPlayerService.getFPlayer(mute.moderatorName())))
                                .build()
                        )
                        .externalModeration(mute)
                        .build();

                yield Optional.of(muteContext);
            }
            case CAPS -> {
                CapsModule capsModuleInstance = capsModule.get();
                Long timestamp = moderationService.getFirstViolationTimestamp(fPlayer.uuid(), capsModuleInstance);
                if (timestamp == null) yield Optional.empty();

                String format = capsModuleInstance.localization(fPlayer).formatRestrict();

                ExternalModeration mute = new ExternalModeration(
                        fPlayer.name(),
                        fileFacade.config().logger().console(),
                        capsModuleInstance.localization(FPlayer.UNKNOWN).formatRestrict(),
                        -1,
                        System.currentTimeMillis(),
                        timestamp,
                        false
                );

                MessageContext muteContext = ExternalModerationMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fPlayer)
                                .message(replacePlaceholders(format, fPlayer, mute))
                                .tagResolver(messagePipeline.targetTag("moderator", fPlayer, fPlayerService.getConsole()))
                                .build()
                        )
                        .externalModeration(mute)
                        .build();

                yield Optional.of(muteContext);
            }
            case FLOOD -> {
                FloodModule floodModuleInstance = floodModule.get();
                Long timestamp = moderationService.getFirstViolationTimestamp(fPlayer.uuid(), floodModuleInstance);
                if (timestamp == null) yield Optional.empty();

                String format = floodModuleInstance.localization(fPlayer).formatRestrict();

                ExternalModeration mute = new ExternalModeration(
                        fPlayer.name(),
                        fileFacade.config().logger().console(),
                        floodModuleInstance.localization(FPlayer.UNKNOWN).formatRestrict(),
                        -1,
                        System.currentTimeMillis(),
                        timestamp,
                        false
                );

                MessageContext muteContext = ExternalModerationMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fPlayer)
                                .message(replacePlaceholders(format, fPlayer, mute))
                                .tagResolver(messagePipeline.targetTag("moderator", fPlayer, fPlayerService.getConsole()))
                                .build()
                        )
                        .externalModeration(mute)
                        .build();

                yield Optional.of(muteContext);
            }
            case NEWBIE -> {
                NewbieModule newbieModuleInstance = newbieModule.get();

                ExternalModeration mute = newbieModuleInstance.getModeration(fPlayer);
                if (mute == null) yield Optional.empty();

                String format = newbieModuleInstance.localization(fPlayer).formatRestrict();

                MessageContext muteContext = ExternalModerationMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fPlayer)
                                .message(replacePlaceholders(format, fPlayer, mute))
                                .tagResolver(messagePipeline.targetTag("moderator", fPlayer, fPlayerService.getConsole()))
                                .build()
                        )
                        .externalModeration(mute)
                        .build();

                yield Optional.of(muteContext);
            }
            case SWEAR -> {
                SwearModule swearModuleInstance = swearModule.get();
                Long timestamp = moderationService.getFirstViolationTimestamp(fPlayer.uuid(), swearModuleInstance);
                if (timestamp == null) yield Optional.empty();

                String format = swearModuleInstance.localization(fPlayer).formatRestrict();

                ExternalModeration mute = new ExternalModeration(
                        fPlayer.name(),
                        fileFacade.config().logger().console(),
                        swearModuleInstance.localization(FPlayer.UNKNOWN).formatRestrict(),
                        -1,
                        System.currentTimeMillis(),
                        timestamp,
                        false
                );

                MessageContext muteContext = ExternalModerationMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fPlayer)
                                .message(replacePlaceholders(format, fPlayer, mute))
                                .tagResolver(messagePipeline.targetTag("moderator", fPlayer, fPlayerService.getConsole()))
                                .build()
                        )
                        .externalModeration(mute)
                        .build();

                yield Optional.of(muteContext);
            }
            default -> Optional.empty();
        };
    }

    @Override
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

    @Override
    public String replacePlaceholders(String message, FPlayer fReceiver, ExternalModeration moderation) {
        return replacePlaceholders(message, fReceiver, moderation.moderationId(), moderation.date(), moderation.time(), moderation.reason(), moderation.permanent());
    }

    @Override
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

    @Override
    public String replacePlaceholders(String message, String moderationId, String reason, String date, String time, String timeLeft) {
        return StringUtils.replaceEach(message,
                new String[]{"<id>", "<reason>", "<date>", "<time>", "<time_left>"},
                new String[]{moderationId, Strings.CS.replace(reason, "\\", "\\\\"), date, time, timeLeft}
        );
    }
}