package net.flectone.pulse.module.command.kick;

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
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.CommandModuleController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.incendo.cloud.context.CommandContext;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class KickModule implements AbstractModuleCommand<Localization.Command.Kick> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final CommandParserProvider commandParserProvider;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final CommandModuleController commandModuleController;

    @Override
    public void onEnable() {
        String promptPlayer = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::player);
        String promptMessage = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::message);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptPlayer, commandParserProvider.playerParser())
                .optional(promptMessage, commandParserProvider.nativeMessageParser())
        );
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

        if (config().checkGroupWeight() && !fPlayerService.hasHigherGroupThan(fPlayer, fTarget)) {
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

        Moderation kick = moderationService.kick(fTarget, reason, fPlayer.id());
        if (kick == null) return;

        kick(fPlayer, fTarget, kick);

        messageDispatcher.dispatch(this, ModerationMetadata.<Localization.Command.Kick>builder()
                .base(EventMetadata.<Localization.Command.Kick>builder()
                        .sender(fTarget)
                        .format((fReceiver, localization) ->
                                moderationMessageFormatter.replacePlaceholders(localization.server(), fReceiver, kick)
                        )
                        .destination(config().destination())
                        .range(config().range())
                        .sound(soundOrThrow())
                        .proxy(dataOutputStream -> dataOutputStream.writeAsJson(kick))
                        .integration(string ->
                                moderationMessageFormatter.replacePlaceholders(string, FPlayer.UNKNOWN, kick)
                        )
                        .tagResolvers(fResolver -> new TagResolver[]{
                                messagePipeline.targetTag("moderator", fResolver, fPlayer)
                        })
                        .build()
                )
                .moderation(kick)
                .build()
        );

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
    public Localization.Command.Kick localization(FEntity sender) {
        return fileFacade.localization(sender).command().kick();
    }

    public void kick(FEntity fModerator, FPlayer fTarget, Moderation kick) {
        if (fModerator == null) return;
        if (moduleController.isDisabledFor(this, fModerator)) return;

        String format = moderationMessageFormatter.replacePlaceholders(localization(fTarget).person(), fTarget, kick);
        MessageContext messageContext = messagePipeline.createContext(fTarget, format)
                .addTagResolver(messagePipeline.targetTag("moderator", fTarget, fModerator));

        platformPlayerAdapter.kick(fTarget, messagePipeline.build(messageContext));
    }
}
