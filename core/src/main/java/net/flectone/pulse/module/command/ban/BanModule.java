package net.flectone.pulse.module.command.ban;

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
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.ban.listener.PulseBanListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.type.tuple.Pair;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BanModule implements ModuleCommand<Localization.Command.Ban> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final MessagePipeline messagePipeline;
    private final ProxySender proxySender;
    private final ListenerRegistry listenerRegistry;
    private final CommandParserProvider commandParserProvider;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;

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

        listenerRegistry.register(PulseBanListener.class);
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
        Pair<Long, String> timeReasonPair = optionalTime.orElse(Pair.of(-1L, null));

        long time = timeReasonPair.first();
        String reason = timeReasonPair.second();

        if (!moderationService.isAllowedTime(fPlayer, time, config().timeLimits())) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Ban>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ban::nullTime)
                    .build()
            );

            return;
        }

        ban(fPlayer, target, time, reason);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_BAN;
    }

    @Override
    public Command.Ban config() {
        return fileFacade.command().ban();
    }

    @Override
    public Permission.Command.Ban permission() {
        return fileFacade.permission().command().ban();
    }

    @Override
    public Localization.Command.Ban localization(FEntity sender) {
        return fileFacade.localization(sender).command().ban();
    }

    public void ban(FPlayer fPlayer, String target, long time, String reason) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Ban>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ban::nullPlayer)
                    .build()
            );

            return;
        }

        if (config().checkGroupWeight() && !fPlayerService.hasHigherGroupThan(fPlayer, fTarget)) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Ban>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ban::lowerWeightGroup)
                    .build()
            );
            return;
        }

        long databaseTime = time != -1 ? time + System.currentTimeMillis() : -1;

        Moderation ban = moderationService.ban(fTarget, databaseTime, reason, fPlayer.id());
        if (ban == null) return;

        proxySender.send(fTarget, ModuleName.SYSTEM_BAN);

        kick(fPlayer, fTarget, ban);

        messageDispatcher.dispatch(this, ModerationMetadata.<Localization.Command.Ban>builder()
                .base(EventMetadata.<Localization.Command.Ban>builder()
                        .sender(fTarget)
                        .format((fReceiver, localization) ->
                                moderationMessageFormatter.replacePlaceholders(localization.server(), fReceiver, ban)
                        )
                        .range(config().range())
                        .destination(config().destination())
                        .sound(soundOrThrow())
                        .proxy(dataOutputStream ->
                                dataOutputStream.writeAsJson(ban)
                        )
                        .integration(string ->
                                moderationMessageFormatter.replacePlaceholders(string, FPlayer.UNKNOWN, ban)
                        )
                        .tagResolvers(fResolver -> new TagResolver[]{messagePipeline.targetTag("moderator", fResolver, fPlayer)})
                        .build()
                )
                .moderation(ban)
                .build()
        );
    }

    public void kick(FEntity fModerator, FPlayer fTarget, Moderation ban) {
        if (fModerator == null) return;
        if (moduleController.isDisabledFor(this, fModerator)) return;

        Localization.Command.Ban localization = localization(fTarget);
        String formatPlayer = moderationMessageFormatter.replacePlaceholders(localization.person(), fTarget, ban);

        MessageContext messageContext = messagePipeline.createContext(fModerator, fTarget, formatPlayer)
                .addTagResolver(messagePipeline.targetTag("moderator", fTarget, fModerator));

        Component kickMessage = messagePipeline.build(messageContext);

        platformPlayerAdapter.kick(fTarget, kickMessage);
    }
}
