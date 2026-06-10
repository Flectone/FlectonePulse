package net.flectone.pulse.module.command.kick;

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
import net.flectone.pulse.model.event.ModerationMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.kick.listener.KickProxyMessageListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
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
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class KickModule implements ModuleCommand<Localization.Command.Kick> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final CommandParserProvider commandParserProvider;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final ProxySender proxySender;
    private final ProxyRegistry proxyRegistry;
    private final ListenerRegistry listenerRegistry;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        String promptPlayer = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::player);
        String promptMessage = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::message);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptPlayer, config().filterByServer() ? commandParserProvider.platformPlayerParser() : commandParserProvider.playerParser())
                .optional(promptMessage, commandParserProvider.nativeMessageParser())
        );

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(KickProxyMessageListener.class);
        }
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String playerName = commandModuleController.getArgument(this, commandContext, 0);
        FPlayer fTarget = fPlayerService.getFPlayer(playerName);
        if (!fTarget.isOnline()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Kick>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Kick::nullPlayer)
                    .build()
            );

            return;
        }

        if (config().checkGroupWeight() && !moderationService.hasHigherGroupThan(fPlayer, fTarget)) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Kick>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Kick::lowerWeightGroup)
                    .build()
            );
            return;
        }

        String promptMessage = commandModuleController.getPrompt(this, 1);
        Optional<String> optionalReason = commandContext.optional(promptMessage);
        String reason = optionalReason.orElse(null);

        Moderation moderation = moderationService.kick(fTarget, reason, fPlayer.id());
        if (moderation == null) return;

        if (!config().filterByServer()) {
            proxySender.send(fTarget, ModuleName.UPDATE_CACHE_KICK, dataOutputStream -> dataOutputStream.writeAsJson(moderation));
        }

        EventMetadata.Builder<Localization.Command.Kick> baseMetadataBuilder = EventMetadata.<Localization.Command.Kick>builder()
                .sender(fTarget)
                .format((fReceiver, localization) ->
                        moderationMessageFormatter.replacePlaceholders(localization.server(), fReceiver, moderation)
                )
                .destination(config().destination())
                .range(config().range())
                .sound(soundOrThrow())
                .proxy(dataOutputStream -> dataOutputStream.writeAsJson(moderation))
                .integration(string ->
                        moderationMessageFormatter.replacePlaceholders(string, FPlayer.UNKNOWN, moderation)
                )
                .tagResolvers(fResolver -> new TagResolver[]{
                        messagePipeline.targetTag("moderator", fResolver, fPlayer)
                });

        if (config().range().is(Range.Type.PLAYER)) {
            baseMetadataBuilder.receivers(List.of(fPlayer, fPlayerService.getConsole()));
        }

        messageDispatcher.dispatch(this, ModerationMetadata.<Localization.Command.Kick>builder()
                .base(baseMetadataBuilder.build())
                .moderation(moderation)
                .build()
        );

        kick(moderation);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_KICK;
    }

    @Override
    public Command.Kick config() {
        return fileFacade.command().kick();
    }

    @Override
    public Permission.Command.Kick permission() {
        return fileFacade.permission().command().kick();
    }

    @Override
    public Localization.Command.Kick localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().kick();
    }

    public void kick(Moderation kick) {
        FPlayer fModerator = fPlayerService.getFPlayer(kick.moderator());
        if (moduleController.isDisabledFor(this, fModerator)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(kick.player());
        if (!platformPlayerAdapter.isOnline(fTarget)) return;

        String format = moderationMessageFormatter.replacePlaceholders(localization(fTarget).person(), fTarget, kick);
        platformPlayerAdapter.kick(fTarget, messagePipeline.build(MessageContext.builder()
                .sender(fTarget)
                .message(format)
                .tagResolver(messagePipeline.targetTag("moderator", fTarget, fModerator))
                .build()
        ));
    }
}
