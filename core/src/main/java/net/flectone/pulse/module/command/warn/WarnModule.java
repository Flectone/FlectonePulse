package net.flectone.pulse.module.command.warn;

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
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.warn.listener.WarnProxyMessageListener;
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
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.incendo.cloud.context.CommandContext;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WarnModule implements ModuleCommand<Localization.Command.Warn> {

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
    private final ModuleCommandController commandModuleController;
    private final ProxyRegistry proxyRegistry;
    private final ListenerRegistry listenerRegistry;
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

        long time = timeReasonPair.getLeft() == -1 ? Duration.ofHours(1).toMillis() : timeReasonPair.getLeft();

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

        if (config().checkGroupWeight() && !moderationService.hasHigherGroupThan(fPlayer, fTarget)) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Warn>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Warn::lowerWeightGroup)
                    .build()
            );
            return;
        }

        long databaseTime = time + System.currentTimeMillis();
        String reason = timeReasonPair.getRight();

        Moderation moderation = moderationService.warn(fTarget, databaseTime, reason, fPlayer.id());
        if (moderation == null) return;

        if (!config().filterByServer()) {
            proxySender.send(fTarget, ModuleName.UPDATE_CACHE_WARN, dataOutputStream -> dataOutputStream.writeAsJson(moderation));
        }

        EventMetadata.Builder<Localization.Command.Warn> baseMetadataBuilder = EventMetadata.<Localization.Command.Warn>builder()
                .sender(fTarget)
                .format((fReceiver, localization) ->
                        moderationMessageFormatter.replacePlaceholders(localization.server(), fReceiver, moderation)
                )
                .range(config().range())
                .destination(config().destination())
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

        messageDispatcher.dispatch(this, ModerationMetadata.<Localization.Command.Warn>builder()
                .base(baseMetadataBuilder.build())
                .moderation(moderation)
                .build()
        );

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

    public void sendForTarget(Moderation warn) {
        FPlayer fModerator = fPlayerService.getFPlayer(warn.moderator());
        if (moduleController.isDisabledFor(this, fModerator)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(warn.player());
        messageDispatcher.dispatch(this, EventMetadata.<Localization.Command.Warn>builder()
                .sender(fTarget)
                .format(localization -> moderationMessageFormatter.replacePlaceholders(localization.person(), fTarget, warn))
                .tagResolvers(fResolver -> new TagResolver[]{
                        messagePipeline.targetTag("moderator", fResolver, fModerator)
                })
                .build()
        );

        int countWarns = moderationService.getTotalValidCount(fTarget, Moderation.Type.WARN, moderationService.getServer(Moderation.Type.WARN));

        String action = config().actions().get(countWarns);
        if (StringUtils.isEmpty(action)) return;

        platformServerAdapter.dispatchCommand(Strings.CS.replace(action, "<target>", fTarget.name()));
    }
}
