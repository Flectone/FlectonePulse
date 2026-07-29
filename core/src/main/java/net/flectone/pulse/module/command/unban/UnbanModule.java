package net.flectone.pulse.module.command.unban;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMessageFormat;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.message.context.ModerationMessageContext;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.unban.listener.UnbanProxyMessageListener;
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
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UnbanModule implements ModuleCommand {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final CommandParserProvider commandParserProvider;
    private final ProxySender proxySender;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final ProxyRegistry proxyRegistry;
    private final ListenerRegistry listenerRegistry;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        String promptPlayer = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::player);
        String promptReason = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::reason);
        commandModuleController.registerCommand(this, manager -> manager
                .permission(permission().name())
                .required(promptPlayer, commandParserProvider.bannedParser())
                .optional(promptReason, commandParserProvider.messageParser())
        );

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(UnbanProxyMessageListener.class);
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
        Optional<String> optionalReason = commandContext.optional(promptReason);
        String reason = optionalReason.orElse("");
        String[] reasonWords = reason.split(" ");

        int id = -1;
        if (reasonWords.length > 0 && StringUtils.isNumeric(reasonWords[0])) {
            id = Integer.parseInt(reasonWords[0]);
            reason = StringUtils.join(reasonWords, " ", 1, reasonWords.length);
        }

        unban(fPlayer, target, id, StringUtils.isEmpty(reason) ? null : reason);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_UNBAN;
    }

    @Override
    public Command.Unban config() {
        return fileFacade.command().unban();
    }

    @Override
    public Permission.Command.Unban permission() {
        return fileFacade.permission().command().unban();
    }

    @Override
    public Localization.Command.Unban localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().unban();
    }

    public void unban(FPlayer fPlayer, String target, int id, String reason) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

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

        if (!moderationService.hasValid(fTarget, Moderation.Type.BAN, id)) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).nullId())
                            .build()
                    )
                    .build()
            );
            return;
        }

        Moderation unban = moderationService.remove(fPlayer, fTarget, Moderation.Type.BAN, id, reason);
        if (unban == null) return;

        if (!fileFacade.command().ban().filterByServer()) {
            proxySender.send(fTarget, ModuleName.UPDATE_CACHE_BAN, dataOutputStream -> dataOutputStream.writeAsJson(unban));
        }

        EventMetadata.Builder baseMetadataBuilder = EventMetadata.builder()
                .destination(config().destination())
                .range(config().range())
                .sound(soundOrThrow())
                .messageContext(fResolver -> ModerationMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fTarget)
                                .receiver(fResolver)
                                .message(moderationMessageFormatter.replacePlaceholders(localization(fResolver).format(), fResolver, unban))
                                .tagResolver(messagePipeline.targetTag("moderator", fResolver, fPlayer))
                                .build()
                        )
                        .moderation(unban)
                        .build()
                )
                .proxy(dataOutputStream ->
                        dataOutputStream.writeAsJson(unban)
                )
                .integration(() -> IntegrationMessageFormat.builder()
                        .format(string -> moderationMessageFormatter.replacePlaceholders(string, FPlayer.UNKNOWN, unban))
                        .build()
                );

        if (config().range().is(Range.Type.PLAYER)) {
            baseMetadataBuilder.filter(List.of(fPlayer, fPlayerService.getConsole()));
        }

        messageDispatcher.dispatch(this, baseMetadataBuilder.build());
    }
}
