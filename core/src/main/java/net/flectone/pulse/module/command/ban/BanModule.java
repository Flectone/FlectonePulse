package net.flectone.pulse.module.command.ban;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.ModerationMetadata;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.ban.listener.BanPulseListener;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.type.tuple.Pair;

import java.util.Optional;
import java.util.function.BiFunction;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BanModule extends AbstractModuleCommand<Localization.Command.Ban> {

    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final MessagePipeline messagePipeline;
    private final ProxySender proxySender;
    private final ListenerRegistry listenerRegistry;
    private final CommandParserProvider commandParserProvider;
    private final IntegrationModule integrationModule;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        String promptReason = addPrompt(1, Localization.Command.Prompt::getReason);
        String promptTime = addPrompt(2, Localization.Command.Prompt::getTime);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().getName())
                .required(promptPlayer, commandParserProvider.playerParser(config().isSuggestOfflinePlayers()))
                .optional(promptTime + " " + promptReason, commandParserProvider.durationReasonParser())
        );

        listenerRegistry.register(BanPulseListener.class);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String target = getArgument(commandContext, 0);
        String promptReason = getPrompt(1);
        String promptTime = getPrompt(2);

        Optional<Pair<Long, String>> optionalTime = commandContext.optional(promptTime + " " + promptReason);
        Pair<Long, String> timeReasonPair = optionalTime.orElse(Pair.of(-1L, null));

        long time = timeReasonPair.first();
        String reason = timeReasonPair.second();

        if (!moderationService.isAllowedTime(fPlayer, time, config().getTimeLimits())) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ban::getNullTime)
                    .build()
            );

            return;
        }

        ban(fPlayer, target, time, reason);
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_BAN;
    }

    @Override
    public Command.Ban config() {
        return fileResolver.getCommand().getBan();
    }

    @Override
    public Permission.Command.Ban permission() {
        return fileResolver.getPermission().getCommand().getBan();
    }

    @Override
    public Localization.Command.Ban localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getBan();
    }

    public void ban(FPlayer fPlayer, String target, long time, String reason) {
        if (isModuleDisabledFor(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ban::getNullPlayer)
                    .build()
            );

            return;
        }

        if (config().isCheckGroupWeight() && !fPlayerService.hasHigherGroupThan(fPlayer, fTarget)) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ban::getLowerWeightGroup)
                    .build()
            );
            return;
        }

        long databaseTime = time != -1 ? time + System.currentTimeMillis() : -1;

        Moderation ban = moderationService.ban(fTarget, databaseTime, reason, fPlayer.getId());
        if (ban == null) return;

        proxySender.send(fTarget, MessageType.SYSTEM_BAN);

        kick(fPlayer, fTarget, ban);

        sendMessage(ModerationMetadata.<Localization.Command.Ban>builder()
                .sender(fTarget)
                .format(buildFormat(ban))
                .moderation(ban)
                .range(config().getRange())
                .destination(config().getDestination())
                .sound(getModuleSound())
                .proxy(dataOutputStream ->
                        dataOutputStream.writeAsJson(ban)
                )
                .integration(string ->
                        moderationMessageFormatter.replacePlaceholders(string, FPlayer.UNKNOWN, ban)
                )
                .build()
        );
    }

    public BiFunction<FPlayer, Localization.Command.Ban, String> buildFormat(Moderation ban) {
        return (fReceiver, message) -> {
            String format = message.getServer();

            return moderationMessageFormatter.replacePlaceholders(format, fReceiver, ban);
        };
    }

    public void kick(FEntity fModerator, FPlayer fTarget, Moderation ban) {
        if (isModuleDisabledFor(fModerator)) return;
        if (fModerator == null) return;

        Localization.Command.Ban localization = localization(fTarget);

        String formatPlayer = localization.getPerson();
        formatPlayer = moderationMessageFormatter.replacePlaceholders(formatPlayer, fTarget, ban);

        fPlayerService.kick(fTarget, messagePipeline.builder(fModerator, fTarget, formatPlayer).build());
    }
}
