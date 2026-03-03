package net.flectone.pulse.module.command.warn;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.ModerationMetadata;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.CommandModuleController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.type.tuple.Pair;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WarnModule extends AbstractModuleCommand<Localization.Command.Warn> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final CommandParserProvider commandParserProvider;
    private final PlatformServerAdapter platformServerAdapter;
    private final ProxySender proxySender;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final CommandModuleController commandModuleController;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptPlayer = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::player);
        String promptReason = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::reason);
        String promptTime = commandModuleController.addPrompt(this, 2, Localization.Command.Prompt::time);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptPlayer, commandParserProvider.playerParser(config().suggestOfflinePlayers()))
                .optional(promptTime + " " + promptReason, commandParserProvider.durationReasonParser())
        );
    }

    @Override
    public void onDisable() {
        super.onDisable();

        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String target = commandModuleController.getArgument(this, commandContext, 0);
        String promptReason = commandModuleController.getPrompt(this, 1);
        String promptTime = commandModuleController.getPrompt(this, 2);

        Optional<Pair<Long, String>> optionalTime = commandContext.optional(promptTime + " " + promptReason);
        Pair<Long, String> timeReasonPair = optionalTime.orElse(Pair.of(Duration.ofHours(1).toMillis(), null));

        long time = timeReasonPair.first() == -1 ? Duration.ofHours(1).toMillis() : timeReasonPair.first();

        if (!moderationService.isAllowedTime(fPlayer, time, config().timeLimits())) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Warn>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Warn::nullTime)
                    .build()
            );

            return;
        }

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Warn>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Warn::nullPlayer)
                    .build()
            );

            return;
        }

        if (config().checkGroupWeight() && !fPlayerService.hasHigherGroupThan(fPlayer, fTarget)) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Warn>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Warn::lowerWeightGroup)
                    .build()
            );
            return;
        }

        long databaseTime = time + System.currentTimeMillis();
        String reason = timeReasonPair.second();

        Moderation warn = moderationService.warn(fTarget, databaseTime, reason, fPlayer.id());
        if (warn == null) return;

        proxySender.send(fTarget, MessageType.SYSTEM_WARN);

        messageDispatcher.dispatch(this, ModerationMetadata.<Localization.Command.Warn>builder()
                .base(EventMetadata.<Localization.Command.Warn>builder()
                        .sender(fTarget)
                        .format((fReceiver, localization) ->
                                moderationMessageFormatter.replacePlaceholders(localization.server(), fReceiver, warn)
                        )
                        .range(config().range())
                        .destination(config().destination())
                        .sound(soundOrThrow())
                        .proxy(dataOutputStream -> dataOutputStream.writeAsJson(warn))
                        .integration(string ->
                                moderationMessageFormatter.replacePlaceholders(string, FPlayer.UNKNOWN, warn)
                        )
                        .tagResolvers(fResolver -> new TagResolver[]{
                                messagePipeline.targetTag("moderator", fResolver, fPlayer)
                        })
                        .build()
                )
                .moderation(warn)
                .build()
        );

        sendForTarget(fPlayer, fTarget, warn);

        List<Moderation> warns = moderationService.getValidWarns(fTarget);
        if (warns.isEmpty()) return;

        int countWarns = warns.stream()
                .filter(Moderation::isActive)
                .toList().size();

        String action = config().actions().get(countWarns);
        if (StringUtils.isEmpty(action)) return;

        platformServerAdapter.dispatchCommand(Strings.CS.replace(action, "<target>", fTarget.name()));
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_WARN;
    }

    @Override
    public Command.Warn config() {
        return fileFacade.command().warn();
    }

    @Override
    public Permission.Command.Warn permission() {
        return fileFacade.permission().command().warn();
    }

    @Override
    public Localization.Command.Warn localization(FEntity sender) {
        return fileFacade.localization(sender).command().warn();
    }

    public void sendForTarget(FEntity fModerator, FPlayer fTarget, Moderation warn) {
        if (moduleController.isDisabledFor(this, fModerator)) return;

        messageDispatcher.dispatch(this, EventMetadata.<Localization.Command.Warn>builder()
                .sender(fTarget)
                .format(localization -> moderationMessageFormatter.replacePlaceholders(localization.person(), fTarget, warn))
                .tagResolvers(fResolver -> new TagResolver[]{
                        messagePipeline.targetTag("moderator", fResolver, fModerator)
                })
                .build()
        );
    }
}
