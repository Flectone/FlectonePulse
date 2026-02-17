package net.flectone.pulse.module.command.mute;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.ModerationMetadata;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.checker.MuteChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.type.tuple.Pair;

import java.time.Duration;
import java.util.Optional;
import java.util.function.BiFunction;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MuteModule extends AbstractModuleCommand<Localization.Command.Mute> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final CommandParserProvider commandParserProvider;
    private final ProxySender proxySender;
    private final MuteChecker muteChecker;
    private final MessagePipeline messagePipeline;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::player);
        String promptReason = addPrompt(1, Localization.Command.Prompt::reason);
        String promptTime = addPrompt(2, Localization.Command.Prompt::time);

        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptPlayer, commandParserProvider.playerParser(config().suggestOfflinePlayers()))
                .optional(promptTime + " " + promptReason, commandParserProvider.durationReasonParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String target = getArgument(commandContext, 0);
        String promptReason = getPrompt(1);
        String promptTime = getPrompt(2);

        Optional<Pair<Long, String>> optionalTime = commandContext.optional(promptTime + " " + promptReason);
        Pair<Long, String> timeReasonPair = optionalTime.orElse(Pair.of(Duration.ofHours(1).toMillis(), null));

        long time = timeReasonPair.first() == -1 ? Duration.ofHours(1).toMillis() : timeReasonPair.first();
        if (!moderationService.isAllowedTime(fPlayer, time, config().timeLimits())) {
            sendErrorMessage(EventMetadata.<Localization.Command.Mute>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Mute::nullTime)
                    .build()
            );

            return;
        }

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            sendErrorMessage(EventMetadata.<Localization.Command.Mute>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Mute::nullPlayer)
                    .build()
            );
            return;
        }

        if (config().checkGroupWeight() && !fPlayerService.hasHigherGroupThan(fPlayer, fTarget)) {
            sendErrorMessage(EventMetadata.<Localization.Command.Mute>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Mute::lowerWeightGroup)
                    .build()
            );
            return;
        }

        long databaseTime = time + System.currentTimeMillis();
        String reason = timeReasonPair.second();

        Moderation mute = moderationService.mute(fTarget, databaseTime, reason, fPlayer.id());
        if (mute == null) return;

        proxySender.send(fTarget, MessageType.SYSTEM_MUTE);

        sendMessage(ModerationMetadata.<Localization.Command.Mute>builder()
                .base(EventMetadata.<Localization.Command.Mute>builder()
                        .sender(fTarget)
                        .format(buildFormat(mute))
                        .range(config().range())
                        .destination(config().destination())
                        .sound(soundOrThrow())
                        .proxy(dataOutputStream -> dataOutputStream.writeAsJson(mute))
                        .integration(string -> moderationMessageFormatter.replacePlaceholders(string, FPlayer.UNKNOWN, mute))
                        .build()
                )
                .moderation(mute)
                .build()
        );

        sendForTarget(fPlayer, fTarget, mute);
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_MUTE;
    }

    @Override
    public Command.Mute config() {
        return fileFacade.command().mute();
    }

    @Override
    public Permission.Command.Mute permission() {
        return fileFacade.permission().command().mute();
    }

    @Override
    public Localization.Command.Mute localization(FEntity sender) {
        return fileFacade.localization(sender).command().mute();
    }

    public MessageContext addTag(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (!(sender instanceof FPlayer fPlayer)) return messageContext;

        return messageContext.addTagResolver(MessagePipeline.ReplacementTag.MUTE_SUFFIX, (argumentQueue, context) -> {
            String suffix = getMuteSuffix(fPlayer, messageContext.receiver());
            if (StringUtils.isEmpty(suffix)) return MessagePipeline.ReplacementTag.emptyTag();
            if (!suffix.contains("%")) return Tag.preProcessParsed(suffix);

            MessageContext suffixContext = messagePipeline.createContext(fPlayer, messageContext.receiver(), suffix)
                    .withFlags(messageContext.flags())
                    .addFlag(MessageFlag.USER_MESSAGE, false);

            return Tag.inserting(messagePipeline.build(suffixContext));
        });
    }

    public String getMuteSuffix(FPlayer fPlayer, FPlayer fReceiver) {
        if (muteChecker.check(fPlayer) == MuteChecker.Status.NONE) return "";

        return localization(fReceiver).suffix();
    }

    public BiFunction<FPlayer, Localization.Command.Mute, String> buildFormat(Moderation mute) {
        return (fReceiver, message) -> moderationMessageFormatter.replacePlaceholders(message.server(), fReceiver, mute);
    }

    public void sendForTarget(FEntity fModerator, FPlayer fReceiver, Moderation mute) {
        if (isModuleDisabledFor(fModerator)) return;

        sendMessage(EventMetadata.<Localization.Command.Mute>builder()
                .sender(fReceiver)
                .format(s -> moderationMessageFormatter.replacePlaceholders(s.person(), fReceiver, mute))
                .build()
        );
    }
}
