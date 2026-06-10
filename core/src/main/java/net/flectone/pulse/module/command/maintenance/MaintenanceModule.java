package net.flectone.pulse.module.command.maintenance;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.maintenance.listener.MaintenanceProxyMessageListener;
import net.flectone.pulse.module.command.maintenance.listener.PulseMaintenanceListener;
import net.flectone.pulse.module.command.maintenance.model.MaintenanceMetadata;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.converter.IconConvertor;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MaintenanceModule implements ModuleCommand<Localization.Command.Maintenance> {

    private final FileFacade fileFacade;
    private final PermissionChecker permissionChecker;
    private final ListenerRegistry listenerRegistry;
    private final @Named("imagePath") Path iconPath;
    private final PlatformServerAdapter platformServerAdapter;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final IconConvertor iconUtil;
    private final CommandParserProvider commandParserProvider;
    private final TaskScheduler taskScheduler;
    private final ModerationService moderationService;
    private final ProxySender proxySender;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final ProxyRegistry proxyRegistry;
    private final SocialService socialService;

    protected String icon;

    @Override
    public void onEnable() {
        listenerRegistry.register(PulseMaintenanceListener.class);

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(MaintenanceProxyMessageListener.class);
        }

        File file = iconPath.resolve("maintenance.png").toFile();

        if (!file.exists()) {
            platformServerAdapter.saveResource("images/maintenance.png");
        }

        icon = iconUtil.convert(file);

        Optional<Moderation> optionalMaintenace = moderationService.getValid(fPlayerService.getConsole(), Moderation.Type.MAINTENANCE);
        if (optionalMaintenace.isPresent()) {
            Moderation maintenance = optionalMaintenace.get();
            kickOnlinePlayers(maintenance);
            unturnLater(maintenance);
        } else {
            moderationService.getValid(fPlayerService.getConsole(), Moderation.Type.UNMAINTENANCE).ifPresent(this::unturnLater);
        }

        String promptType = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::type);
        String promptReason = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::reason);
        String promptTime = commandModuleController.addPrompt(this, 2, Localization.Command.Prompt::time);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .optional(promptType, commandParserProvider.singleMessageParser(), typeSuggestion())
                .optional(promptTime + " " + promptReason, commandParserProvider.durationReasonParser())
        );
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleCommand.super.permissionBuilder().add(permission().join());
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String promptType = commandModuleController.getPrompt(this, 0);
        Optional<String> optionalType = commandContext.optional(promptType);

        // get current state
        boolean isAlreadyTurned = isTurnedOn();

        // command can be used without arguments
        boolean turned = optionalType
                .map(string -> !string.equalsIgnoreCase("end"))
                .orElseGet(() -> !isAlreadyTurned);

        if (turned && isAlreadyTurned || !turned && !isAlreadyTurned) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Maintenance>builder()
                    .sender(fPlayer)
                    .format(localization -> turned ? localization.alreadyTrue() : localization.alreadyFalse())
                    .build()
            );

            return;
        }

        String promptReason = commandModuleController.getPrompt(this, 1);
        String promptTime = commandModuleController.getPrompt(this, 2);
        Optional<Pair<Long, String>> optionalTime = commandContext.optional(promptTime + " " + promptReason);
        Pair<Long, String> timeReasonPair = optionalTime.orElse(Pair.of(-1L, null));

        long time = timeReasonPair.getLeft() == -1 ? -1 : timeReasonPair.getLeft();
        String reason = timeReasonPair.getRight();

        turn(fPlayer, reason, time, turned).ifPresent(this::unturnLater);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_MAINTENANCE;
    }

    @Override
    public Command.Maintenance config() {
        return fileFacade.command().maintenance();
    }

    @Override
    public Permission.Command.Maintenance permission() {
        return fileFacade.permission().command().maintenance();
    }

    @Override
    public Localization.Command.Maintenance localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().maintenance();
    }

    public boolean isAllowed(FPlayer fPlayer) {
        if (!moduleController.isEnable(this)) return true;
        if (!isTurnedOn()) return true;

        return permissionChecker.check(fPlayer, permission().join());
    }

    public boolean isTurnedOn() {
        return moduleController.isEnable(this) && moderationService.hasValid(fPlayerService.getConsole(), Moderation.Type.MAINTENANCE);
    }

    public Optional<Moderation> turn(FPlayer fPlayer, @Nullable String reason, long time, boolean turned) {
        FPlayer fTarget = fPlayerService.getConsole();

        long databaseTime = time != -1 ? time + System.currentTimeMillis() : -1;

        Moderation moderation;
        if (turned) {
            // invalidate all unmaintenance
            moderationService.invalidate(fTarget, Moderation.Type.UNMAINTENANCE, -1);

            // save maintenance for server target (console)
            moderation = moderationService.maintenance(fTarget, databaseTime, reason, fPlayer.id());
        } else {
            moderation = moderationService.remove(fPlayer, fTarget, Moderation.Type.MAINTENANCE, databaseTime,-1, StringUtils.isEmpty(reason) ? "disabled" : reason);
        }

        // skip error
        if (moderation == null) return Optional.empty();

        if (!config().filterByServer()) {
            proxySender.send(fTarget, ModuleName.UPDATE_CACHE_MAINTENANCE, dataOutputStream -> dataOutputStream.writeAsJson(moderation));
        }

        EventMetadata.Builder<Localization.Command.Maintenance> baseMetadataBuilder = EventMetadata.<Localization.Command.Maintenance>builder()
                .sender(fTarget)
                .format((fReceiver, localization) ->
                        moderationMessageFormatter.replacePlaceholders(turned ? localization.formatTrue() : localization.formatFalse(), fReceiver, moderation)
                )
                .range(config().range())
                .destination(config().destination())
                .sound(soundOrThrow())
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeAsJson(moderation);
                    dataOutputStream.writeBoolean(turned);
                })
                .integration(IntegrationMetadata.builder()
                        .messageNames(List.of(name().name() + "_" + String.valueOf(turned).toUpperCase()))
                        .build()
                )
                .tagResolvers(fResolver -> new TagResolver[]{
                        messagePipeline.targetTag("moderator", fResolver, fPlayer)
                });

        if (config().range().is(Range.Type.PLAYER)) {
            baseMetadataBuilder.receivers(List.of(fPlayer, fPlayerService.getConsole()));
        }

        messageDispatcher.dispatch(this, MaintenanceMetadata.<Localization.Command.Maintenance>builder()
                .base(baseMetadataBuilder.build())
                .moderation(moderation)
                .turned(turned)
                .build()
        );

        if (moderation.type() == Moderation.Type.MAINTENANCE) {
            kickOnlinePlayers(moderation);
        }

        return Optional.of(moderation);
    }

    public void kickOnlinePlayers(@NonNull Moderation maintenance) {
        FPlayer fModerator = fPlayerService.getFPlayer(maintenance.moderator());
        if (moduleController.isDisabledFor(this, fModerator)) return;

        fPlayerService.getOnlineFPlayers().stream()
                .filter(filter -> !permissionChecker.check(filter, permission().join()))
                .forEach(fReceiver -> {
                    Localization.Command.Maintenance localization = localization(fReceiver);
                    String formatPlayer = moderationMessageFormatter.replacePlaceholders(localization.person(), fReceiver, maintenance);

                    platformPlayerAdapter.kick(fReceiver, messagePipeline.build(MessageContext.builder()
                            .sender(fModerator)
                            .receiver(fReceiver)
                            .message(formatPlayer)
                            .tagResolver(messagePipeline.targetTag("moderator", fReceiver, fModerator))
                            .build()
                    ));
                });
    }

    private void unturnLater(Moderation maintenance) {
        long time = maintenance.time();
        if (time == -1) return;

        // we need to check this before it is invalid in database
        long delay = (time - System.currentTimeMillis()) / TimeFormatter.MULTIPLIER - 10L;
        if (delay < 0) return;

        taskScheduler.runAsyncLater(() -> {
            Optional<Moderation> currentModeration = moderationService.getValid(fPlayerService.getConsole(), maintenance.type());
            if (currentModeration.isEmpty()) return;

            Moderation currentMaintenance = currentModeration.get();
            if (!currentMaintenance.equals(maintenance)) return;

            turn(fPlayerService.getFPlayer(currentMaintenance.moderator()), null, -1, maintenance.type() != Moderation.Type.MAINTENANCE);
        }, delay);
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> typeSuggestion() {
        return (_, _) -> List.of(
                Suggestion.suggestion("start"),
                Suggestion.suggestion("end")
        );
    }

}
