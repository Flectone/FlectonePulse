package net.flectone.pulse.module.command.unwarn;

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
import net.flectone.pulse.model.event.UnModerationMetadata;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.unwarn.listener.UnwarnProxyMessageListener;
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
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UnwarnModule implements ModuleCommand<Localization.Command.Unwarn> {

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
                .required(promptPlayer, commandParserProvider.warnedParser())
                .optional(promptReason, commandParserProvider.messageParser())
        );

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(UnwarnProxyMessageListener.class);
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

        unwarn(fPlayer, target, id, StringUtils.isEmpty(reason) ? null : reason);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_UNWARN;
    }

    @Override
    public Command.Unwarn config() {
        return fileFacade.command().unwarn();
    }

    @Override
    public Permission.Command.Unwarn permission() {
        return fileFacade.permission().command().unwarn();
    }

    @Override
    public Localization.Command.Unwarn localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().unwarn();
    }

    public void unwarn(FPlayer fPlayer, String target, int id, String reason) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Unwarn>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unwarn::nullPlayer)
                    .build()
            );

            return;
        }

        if (config().checkGroupWeight() && !moderationService.hasHigherGroupThan(fPlayer, fTarget)) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Unwarn>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unwarn::lowerWeightGroup)
                    .build()
            );

            return;
        }

        if (!moderationService.hasValid(fTarget, Moderation.Type.WARN, id)) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Unwarn>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unwarn::nullId)
                    .build()
            );

            return;
        }

        Moderation moderation = moderationService.remove(fPlayer, fTarget, Moderation.Type.WARN, id, reason);
        if (moderation == null) return;

        if (!fileFacade.command().warn().filterByServer()) {
            proxySender.send(fTarget, ModuleName.UPDATE_CACHE_WARN, dataOutputStream -> dataOutputStream.writeAsJson(moderation));
        }

        EventMetadata.Builder<Localization.Command.Unwarn> baseMetadataBuilder = EventMetadata.<Localization.Command.Unwarn>builder()
                .sender(fTarget)
                .format((fReceiver, localization) ->
                        moderationMessageFormatter.replacePlaceholders(localization.format(), fReceiver, moderation)
                )
                .destination(config().destination())
                .range(config().range())
                .sound(soundOrThrow())
                .proxy(dataOutputStream ->
                        dataOutputStream.writeAsJson(moderation)
                )
                .integration(string ->
                        moderationMessageFormatter.replacePlaceholders(string, FPlayer.UNKNOWN, moderation)
                )
                .tagResolvers(fResolver -> new TagResolver[]{
                        messagePipeline.targetTag("moderator", fResolver, fPlayer)
                });

        if (config().range().is(Range.Type.PLAYER)) {
            baseMetadataBuilder.receivers(List.of(fPlayer, fPlayerService.getConsole()));
        }

        messageDispatcher.dispatch(this, UnModerationMetadata.<Localization.Command.Unwarn>builder()
                .base(baseMetadataBuilder.build())
                .unmoderation(moderation)
                .build()
        );
    }
}
