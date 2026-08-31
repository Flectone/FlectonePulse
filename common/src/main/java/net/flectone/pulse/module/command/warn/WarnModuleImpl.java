package net.flectone.pulse.module.command.warn;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMessageFormat;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.message.context.ModerationMessageContext;
import net.flectone.pulse.model.value.Moderation;
import net.flectone.pulse.model.value.Pair;
import net.flectone.pulse.model.value.Range;
import net.flectone.pulse.module.command.warn.listener.WarnProxyMessageListener;
import net.flectone.pulse.parser.integer.DurationReasonParser;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.service.SocialService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WarnModuleImpl implements WarnModule {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final CommandParserProvider commandParserProvider;
    private final DurationReasonParser durationReasonParser;
    private final PlatformServerAdapter platformServerAdapter;
    private final ProxySender proxySender;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final ProxyRegistry proxyRegistry;
    private final ListenerRegistry listenerRegistry;
    private final SocialService socialService;
    private final Gson gson;

    @Override
    public void onEnable() {
        String promptPlayer = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::player);
        String promptReason = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::reason);
        String promptTime = commandModuleController.addPrompt(this, 2, Localization.Command.Prompt::time);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptPlayer, commandParserProvider.playerParser(config().suggestOfflinePlayers()))
                .optional(promptTime + " " + promptReason, commandParserProvider.durationReasonParser())
        );

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(WarnProxyMessageListener.class);
        }
    }

    @Override
    public void onDisable() {
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

        long time = timeReasonPair.getLeft() == -1
                ? durationReasonParser.parseTime(config().reasonTimes().getTime(timeReasonPair.getRight()))
                : timeReasonPair.getLeft();

        if (!moderationService.isAllowedTime(fPlayer, time, config().timeLimits())) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).nullTime())
                            .build()
                    )
                    .build()
            );

            return;
        }

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).nullPlayer())
                            .build()
                    )
                    .build()
            );

            return;
        }

        if (config().checkGroupWeight() && !moderationService.hasHigherGroupThan(fPlayer, fTarget)) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).lowerWeightGroup())
                            .build()
                    )
                    .build()
            );
            return;
        }

        long databaseTime = time + System.currentTimeMillis();
        String reason = timeReasonPair.getRight();

        Moderation moderation = moderationService.warn(fTarget, databaseTime, reason, fPlayer.id());
        if (moderation == null) return;

        if (!config().filterByServer()) {
            proxySender.send(fTarget, ModuleName.UPDATE_CACHE_WARN, dataOutputStream -> dataOutputStream.writeUTF(gson.toJson(moderation)));
        }

        EventMetadata.Builder baseMetadataBuilder = EventMetadata.builder()
                .range(config().range())
                .destination(config().destination())
                .sound(soundOrThrow())
                .messageContext(fResolver -> ModerationMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fTarget)
                                .receiver(fResolver)
                                .message(moderationMessageFormatter.replacePlaceholders(localization(fResolver).server(), fResolver, moderation))
                                .tagResolver(messagePipeline.targetTag("moderator", fResolver, fPlayer))
                                .build()
                        )
                        .moderation(moderation)
                        .build()
                )
                .proxy(dataOutputStream -> dataOutputStream.writeUTF(gson.toJson(moderation)))
                .integration(() -> IntegrationMessageFormat.builder()
                        .format(string -> moderationMessageFormatter.replacePlaceholders(string, FPlayer.UNKNOWN, moderation))
                        .build()
                );

        if (config().range().is(Range.Type.PLAYER)) {
            baseMetadataBuilder.filter(List.of(fPlayer, fPlayerService.getConsole()));
        }

        messageDispatcher.dispatch(this, baseMetadataBuilder.build());

        sendForTarget(moderation);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_WARN;
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
    public Localization.Command.Warn localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().warn();
    }

    @Override
    public void sendForTarget(Moderation warn) {
        FPlayer fModerator = fPlayerService.getFPlayer(warn.moderator());
        if (moduleController.isDisabledFor(this, fModerator)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(warn.player());
        messageDispatcher.dispatch(this, EventMetadata.builder()
                .messageContext(fResolver -> ModerationMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fTarget)
                                .receiver(fResolver)
                                .message(moderationMessageFormatter.replacePlaceholders(localization(fResolver).person(), fTarget, warn))
                                .tagResolver(messagePipeline.targetTag("moderator", fResolver, fModerator))
                                .build()
                        )
                        .moderation(warn)
                        .build()
                )
                .build()
        );

        int countWarns = moderationService.getTotalValidCount(fTarget, Moderation.Type.WARN, moderationService.getServer(Moderation.Type.WARN));

        String action = config().actions().get(countWarns);
        if (StringUtils.isEmpty(action)) return;

        platformServerAdapter.dispatchCommand(Strings.CS.replace(action, "<target>", fTarget.name()));
    }
}
