package net.flectone.pulse.module.command.ban;

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
import net.flectone.pulse.module.command.ban.listener.BanProxyMessageListener;
import net.flectone.pulse.module.command.ban.listener.PulseBanListener;
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
import org.apache.commons.lang3.tuple.Pair;
import org.incendo.cloud.context.CommandContext;
import org.jspecify.annotations.NonNull;

import java.util.List;
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
    private final ProxyRegistry proxyRegistry;
    private final ListenerRegistry listenerRegistry;
    private final CommandParserProvider commandParserProvider;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final SocialService socialService;

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
            listenerRegistry.register(BanProxyMessageListener.class);
        }

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

        long time = timeReasonPair.getLeft();
        String reason = timeReasonPair.getRight();

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
    public Localization.Command.Ban localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().ban();
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

        if (config().checkGroupWeight() && !moderationService.hasHigherGroupThan(fPlayer, fTarget)) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Ban>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ban::lowerWeightGroup)
                    .build()
            );
            return;
        }

        long databaseTime = time != -1 ? time + System.currentTimeMillis() : -1;

        Moderation moderation = moderationService.ban(fTarget, databaseTime, reason, fPlayer.id());
        if (moderation == null) return;

        if (!config().filterByServer()) {
            proxySender.send(fTarget, ModuleName.UPDATE_CACHE_BAN, dataOutputStream -> dataOutputStream.writeAsJson(moderation));
        }

        EventMetadata.Builder<Localization.Command.Ban> baseMetadataBuilder = EventMetadata.<Localization.Command.Ban>builder()
                .sender(fTarget)
                .format((fReceiver, localization) ->
                        moderationMessageFormatter.replacePlaceholders(localization.server(), fReceiver, moderation)
                )
                .range(config().range())
                .destination(config().destination())
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

        messageDispatcher.dispatch(this, ModerationMetadata.<Localization.Command.Ban>builder()
                .base(baseMetadataBuilder.build())
                .moderation(moderation)
                .build()
        );

        kick(moderation);
    }

    public void kick(@NonNull Moderation ban) {
        FPlayer fModerator = fPlayerService.getFPlayer(ban.moderator());
        if (moduleController.isDisabledFor(this, fModerator)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(ban.player());
        if (!platformPlayerAdapter.isOnline(fTarget)) return;

        Localization.Command.Ban localization = localization(fTarget);
        String formatPlayer = moderationMessageFormatter.replacePlaceholders(localization.person(), fTarget, ban);

        platformPlayerAdapter.kick(fTarget, messagePipeline.build(MessageContext.builder()
                .sender(fModerator)
                .receiver(fTarget)
                .message(formatPlayer)
                .tagResolver(messagePipeline.targetTag("moderator", fTarget, fModerator))
                .build())
        );
    }
}
