package net.flectone.pulse.module.command.whitelist;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMetadata;
import net.flectone.pulse.model.event.ModerationMetadata;
import net.flectone.pulse.model.event.UnModerationMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.whitelist.listener.PulseWhitelistListener;
import net.flectone.pulse.module.command.whitelist.listener.WhitelistProxyMessageListener;
import net.flectone.pulse.module.command.whitelist.model.WhitelistMetadata;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ModerationListSender;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.parser.string.UUIDParser;
import net.flectone.pulse.processing.resolver.ProfileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.checker.ValidNameChecker;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WhitelistModule implements ModuleCommand<Localization.Command.Whitelist> {

    private final AtomicBoolean tickerStarted = new AtomicBoolean(false);

    private final FileFacade fileFacade;
    private final ModuleController moduleController;
    private final CommandParserProvider commandParserProvider;
    private final ModuleCommandController commandModuleController;
    private final MessageDispatcher messageDispatcher;
    private final FPlayerService fPlayerService;
    private final PlatformServerAdapter platformServerAdapter;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PermissionChecker permissionChecker;
    private final ModerationService moderationService;
    private final ListenerRegistry listenerRegistry;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final ModerationListSender moderationListSender;
    private final ProfileResolver profileResolver;
    private final TaskScheduler taskScheduler;
    private final MessagePipeline messagePipeline;
    private final ProxySender proxySender;
    private final UUIDParser uuidParser;
    private final Gson gson;
    private final FLogger fLogger;
    private final ProxyRegistry proxyRegistry;
    private final SocialService socialService;
    private final ValidNameChecker validNameChecker;

    @Override
    public void onEnable() {
        String promptType = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::type);
        String promptPlayer = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::player);
        String promptReason = commandModuleController.addPrompt(this, 2, Localization.Command.Prompt::reason);
        String promptTime = commandModuleController.addPrompt(this, 3, Localization.Command.Prompt::time);

        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptType, commandParserProvider.singleMessageParser(), SuggestionProvider.blockingStrings((_, _) -> List.of("on", "off", "import")))
                .optional(promptTime + " " + promptReason, commandParserProvider.durationReasonParser())
        );

        commandModuleController.registerSubCommand(this, config().subCommandPlayer(), commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptType, commandParserProvider.singleMessageParser(), SuggestionProvider.blockingStrings((_, _) -> List.of("add", "remove", "list")))
                .optional(promptPlayer, commandParserProvider.whitelistedParser())
                .optional(promptTime + " " + promptReason, commandParserProvider.durationReasonParser())
                .handler(this)
        );

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(WhitelistProxyMessageListener.class);
        }

        listenerRegistry.register(PulseWhitelistListener.class);

        if (isTurnedOn()) {
            startKickTicker();
        }
    }

    @Override
    public void onDisable() {
        tickerStarted.set(false);
    }

    @Override
    public Localization.Command.Whitelist localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().whitelist();
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_WHITELIST;
    }

    @Override
    public Command.Whitelist config() {
        return fileFacade.command().whitelist();
    }

    @Override
    public Permission.Command.Whitelist permission() {
        return fileFacade.permission().command().whitelist();
    }

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleCommand.super.permissionBuilder().add(permission().bypass());
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        // get action type
        String type = commandModuleController.getArgument(this, commandContext, 0);
        Action action = Arrays.stream(Action.values())
                .filter(actionType -> actionType.name().equalsIgnoreCase(type))
                .findAny()
                .orElse(null);

        boolean isPlayerCommand = commandContext.command().rootComponent().name().endsWith("player");

        if (action == null
                || (action == Action.ON || action == Action.OFF || action == Action.IMPORT) && isPlayerCommand
                || (action == Action.ADD || action == Action.REMOVE || action == Action.LIST) && !isPlayerCommand) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Whitelist>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Whitelist::nullType)
                    .build()
            );
            return;
        }

        switch (action) {
            case ON, OFF -> {
                boolean turned = action == Action.ON;
                boolean isAlreadyTurned = isTurnedOn();
                if (turned && isAlreadyTurned) {
                    messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Whitelist>builder()
                            .sender(fPlayer)
                            .format(Localization.Command.Whitelist::alreadyOn)
                            .build()
                    );
                    return;
                }

                if (!turned && !isAlreadyTurned) {
                    messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Whitelist>builder()
                            .sender(fPlayer)
                            .format(Localization.Command.Whitelist::alreadyOff)
                            .build()
                    );
                    return;
                }

                Pair<Long, String> timeReasonPair = getTimeReasonArgument(commandContext);

                turn(fPlayer, timeReasonPair.getRight(), timeReasonPair.getLeft(), turned).ifPresent(this::unturnLater);
            }
            case IMPORT -> actionImport(fPlayer, commandContext);
            case ADD -> {
                Optional<String> optionalPlayer = getPlayerNameArgument(fPlayer, commandContext);
                if (optionalPlayer.isPresent()) {
                    String playerName = optionalPlayer.get();

                    Pair<Long, String> timeReasonPair = getTimeReasonArgument(commandContext);

                    actionAdd(fPlayer, timeReasonPair, playerName, null);
                }
            }
            case REMOVE -> actionRemove(fPlayer, commandContext);
            case LIST -> actionList(fPlayer, commandContext);
        }
    }

    @Nullable
    public Moderation add(@NonNull FPlayer fModerator, @NonNull FPlayer fTarget, long time, @Nullable String reason) {
        return moderationService.whitelist(fTarget, time != -1 ? time + System.currentTimeMillis() : -1, reason, fModerator.id());
    }

    public boolean isTurnedOn() {
        return moduleController.isEnable(this) && moderationService.hasValid(fPlayerService.getConsole(), Moderation.Type.WHITELIST);
    }

    public boolean isWhitelisted(FPlayer fPlayer) {
        return moderationService.hasValid(fPlayer, Moderation.Type.WHITELIST);
    }

    private void startKickTicker() {
        if (!tickerStarted.compareAndSet(false, true)) return;

        taskScheduler.runPlayerAsyncTimer(fPlayer -> {
            if (!isTurnedOn()) return;
            if (permissionChecker.check(fPlayer, permission().bypass())) return;
            if (isWhitelisted(fPlayer)) return;

            kickPlayer(fPlayerService.getConsole(), fPlayer);
        }, 20L);
    }

    private Optional<Moderation> turn(FPlayer fPlayer, @Nullable String reason, long time, boolean turned) {
        long databaseTime = time != -1 ? time + System.currentTimeMillis() : -1;

        FPlayer fTarget = fPlayerService.getConsole();

        Moderation moderation;
        if (turned) {
            // invalidate all unwhitelist for server target (console)
            moderationService.invalidate(fTarget, Moderation.Type.UNWHITELIST, -1);

            // save whitelist for server target (console)
            moderation = moderationService.whitelist(fTarget, databaseTime, StringUtils.isEmpty(reason) ? "enabled" : reason, fPlayer.id());
        } else {
            moderation = moderationService.remove(fPlayer, fTarget, Moderation.Type.WHITELIST, databaseTime, -1,  StringUtils.isEmpty(reason) ? "disabled" : reason);
        }

        // skip error
        if (moderation == null) return Optional.empty();

        if (!config().filterByServer()) {
            proxySender.send(fTarget, ModuleName.UPDATE_CACHE_WHITELIST, dataOutputStream -> dataOutputStream.writeAsJson(moderation));
        }

        EventMetadata.Builder<Localization.Command.Whitelist> baseMetadataBuilder = EventMetadata.<Localization.Command.Whitelist>builder()
                .sender(fTarget)
                .format((fReceiver, localization) ->
                        moderationMessageFormatter.replacePlaceholders(turned ? localization.formatOn() : localization.formatOff(), fReceiver, moderation)
                )
                .destination(config().destination())
                .sound(soundOrThrow())
                .range(config().range())
                .proxy(dataOutputStream -> dataOutputStream.writeInt(turned ? Action.ON.ordinal() : Action.OFF.ordinal()))
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

        messageDispatcher.dispatch(this, WhitelistMetadata.<Localization.Command.Whitelist>builder()
                .base(baseMetadataBuilder.build())
                .moderation(moderation)
                .turnedOn(turned)
                .build()
        );

        if (moderation.type() == Moderation.Type.WHITELIST) {
            kickOnlinePlayers(moderation);
            startKickTicker();
        }

        return Optional.of(moderation);
    }

    public void kickOnlinePlayers(@NonNull Moderation moderation) {
        fPlayerService.getOnlineFPlayers().forEach(fReceiver -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(moderation.moderator());

            kickPlayer(fPlayer, fReceiver);
        });
    }

    private void unturnLater(Moderation whitelist) {
        long time = whitelist.time();
        if (time == -1) return;

        // we need to check this before it is invalid in database
        long delay = (time - System.currentTimeMillis()) / TimeFormatter.MULTIPLIER - 10L;
        if (delay < 0) return;

        taskScheduler.runAsyncLater(() -> {
            Optional<Moderation> currentModeration = moderationService.getValid(fPlayerService.getConsole(), whitelist.type());
            if (currentModeration.isEmpty()) return;

            Moderation currentMaintenance = currentModeration.get();
            if (!currentMaintenance.equals(whitelist)) return;

            turn(fPlayerService.getFPlayer(currentMaintenance.moderator()), null, -1, whitelist.type() != Moderation.Type.WHITELIST);
        }, delay);
    }

    private void actionImport(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        File whitelistFile = platformServerAdapter.getWhitelistFile();
        if (!whitelistFile.exists()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Whitelist>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Whitelist::empty)
                    .build()
            );
            return;
        }

        Pair<Long, String> timeReasonPair = getTimeReasonArgument(commandContext);

        try {
            String json = Files.readString(whitelistFile.toPath());
            if (platformServerAdapter.getPlatformType() == PlatformType.HYTALE) {
                gson.fromJson(json, HytaleWhitelist.class).list()
                        .forEach(uuid -> actionAdd(fPlayer, timeReasonPair, uuid, null));
            } else {
                Arrays.stream(gson.fromJson(json, MinecraftWhitelistEntry[].class))
                        .forEach(entry -> actionAdd(fPlayer, timeReasonPair, entry.uuid().toString(), entry.name()));
            }
        } catch (IOException e) {
            fLogger.warning(e);
        }
    }

    private void actionAdd(FPlayer fPlayer, Pair<Long, String> timeReasonPair, String argument, @Nullable String existingName) {
        // save new FPlayer
        FPlayer fTarget = parseFPlayerAndSaveNew(fPlayer, argument, existingName);
        if (fTarget == null) return;

        // save whitelist moderation
        Moderation whitelist = add(fPlayer, fTarget, timeReasonPair.getLeft(), timeReasonPair.getRight());
        if (whitelist == null) return;

        if (!config().filterByServer()) {
            proxySender.send(fTarget, ModuleName.UPDATE_CACHE_WHITELIST);
        }

        EventMetadata.Builder<Localization.Command.Whitelist> baseMetadataBuilder = EventMetadata.<Localization.Command.Whitelist>builder()
                .sender(fTarget)
                .format((fReceiver, localization) ->
                        moderationMessageFormatter.replacePlaceholders(localization.formatAdd(), fReceiver, whitelist)
                )
                .destination(config().destination())
                .sound(soundOrThrow())
                .range(config().range())
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeInt(Action.ADD.ordinal());
                    dataOutputStream.writeAsJson(whitelist);
                })
                .integration(IntegrationMetadata.builder()
                        .format(string -> moderationMessageFormatter.replacePlaceholders(string, FPlayer.UNKNOWN, whitelist))
                        .messageNames(List.of(name().name() + "_ADD"))
                        .build()
                )
                .tagResolvers(fResolver -> new TagResolver[]{
                        messagePipeline.targetTag("moderator", fResolver, fPlayer)
                });

        if (config().range().is(Range.Type.PLAYER)) {
            baseMetadataBuilder.receivers(List.of(fPlayer, fPlayerService.getConsole()));
        }

        messageDispatcher.dispatch(this, ModerationMetadata.<Localization.Command.Whitelist>builder()
                .base(baseMetadataBuilder.build())
                .moderation(whitelist)
                .build()
        );
    }

    private void actionRemove(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        Optional<String> optionalPlayer = getPlayerNameArgument(fPlayer, commandContext);
        if (optionalPlayer.isEmpty()) return;

        String playerName = optionalPlayer.get();

        // check playerName, maybe this is UUID
        UUID uuid = uuidParser.parse(playerName);

        FPlayer fTarget = uuid != null ? fPlayerService.getFPlayer(uuid) : fPlayerService.getFPlayer(playerName);
        if (fTarget.isUnknown()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Whitelist>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Whitelist::nullPlayer)
                    .build()
            );
            return;
        }

        Pair<Long, String> timeReasonPair = getTimeReasonArgument(commandContext);

        long time = timeReasonPair.getLeft();
        String reason = timeReasonPair.getRight();
        if (time != -1) {
            reason = reason != null ? time + " " + reason : String.valueOf(time);
        } else if (reason == null) {
            reason = "";
        }

        String[] reasonWords = reason.split(" ");

        int id;
        if (reasonWords.length > 0 && StringUtils.isNumeric(reasonWords[0])) {
            id = Integer.parseInt(reasonWords[0]);
            reason = StringUtils.join(reasonWords, " ", 1, reasonWords.length);
        } else {
            id = -1;
        }

        if (!moderationService.hasValid(fTarget, Moderation.Type.WHITELIST, id)) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Whitelist>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Whitelist::alreadyRemove)
                    .build()
            );
            return;
        }

        // invalidate whitelist moderations and add unwhitelist moderation
        Moderation unwhitelist = moderationService.remove(fPlayer, fTarget, Moderation.Type.WHITELIST, id, StringUtils.isEmpty(reason) ? "removed" : reason);
        if (unwhitelist == null) return;

        if (!config().filterByServer()) {
            proxySender.send(fTarget, ModuleName.UPDATE_CACHE_WHITELIST);
        }

        EventMetadata.Builder<Localization.Command.Whitelist> baseMetadataBuilder = EventMetadata.<Localization.Command.Whitelist>builder()
                .sender(fTarget)
                .format((fReceiver, localization) ->
                        moderationMessageFormatter.replacePlaceholders(localization.formatRemove(), fReceiver, unwhitelist)
                )
                .destination(config().destination())
                .sound(soundOrThrow())
                .range(config().range())
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeInt(Action.REMOVE.ordinal());
                    dataOutputStream.writeAsJson(unwhitelist);
                })
                .integration(IntegrationMetadata.builder()
                        .format(string -> moderationMessageFormatter.replacePlaceholders(string, FPlayer.UNKNOWN, unwhitelist))
                        .messageNames(List.of(name().name() + "_REMOVE"))
                        .build()
                )
                .tagResolvers(fResolver -> new TagResolver[]{
                        messagePipeline.targetTag("moderator", fResolver, fPlayer)
                });

        if (config().range().is(Range.Type.PLAYER)) {
            baseMetadataBuilder.receivers(List.of(fPlayer, fPlayerService.getConsole()));
        }

        messageDispatcher.dispatch(this, UnModerationMetadata.<Localization.Command.Whitelist>builder()
                .base(baseMetadataBuilder.build())
                .unmoderation(unwhitelist)
                .build()
        );

        kickPlayer(fPlayer, fTarget);
    }

    private void actionList(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        moderationListSender.send(
                this,
                fPlayer,
                commandContext,
                Moderation.Type.WHITELIST,
                1,
                config().perPage(),
                "/" + commandModuleController.getCommandName(this) + config().subCommandPlayer() + " list",
                fTarget -> "/" + commandModuleController.getCommandName(this) + config().subCommandPlayer() + " remove " + fTarget.uuid() + " <id>"
        );
    }

    private Optional<String> getPlayerNameArgument(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        String promptPlayer = commandModuleController.getPrompt(this, 1);
        Optional<String> optionalPlayer = commandContext.optional(promptPlayer);
        if (optionalPlayer.isEmpty()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Whitelist>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Whitelist::nullPlayer)
                    .build()
            );
        }

        return optionalPlayer;
    }

    private Pair<Long, String> getTimeReasonArgument(CommandContext<FPlayer> commandContext) {
        String promptReason = commandModuleController.getPrompt(this, 2);
        String promptTime = commandModuleController.getPrompt(this, 3);

        Optional<Pair<Long, String>> optionalTime = commandContext.optional(promptTime + " " + promptReason);
        return optionalTime.orElse(Pair.of(-1L, null));
    }

    @Nullable
    private FPlayer parseFPlayerAndSaveNew(FPlayer fPlayer, String uuidOrName, @Nullable String name) {
        UUID uuid = uuidParser.parse(uuidOrName);
        boolean isUuid = uuid != null;

        FPlayer fTarget = isUuid ? fPlayerService.getFPlayer(uuid) : fPlayerService.getFPlayer(uuidOrName);
        if (fTarget.isConsole() || !isUuid && !validNameChecker.check(uuidOrName)) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Whitelist>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Whitelist::nullPlayer)
                    .build()
            );
            return null;
        }

        if (!fTarget.isUnknown() && config().checkDuplicate() && isWhitelisted(fTarget)) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Whitelist>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Whitelist::alreadyAdd)
                    .build()
            );
            return null;
        }

        String resolvedName;
        if (isUuid) {
            resolvedName = platformServerAdapter.isOnlineMode()
                    ? profileResolver.resolveOnlineName(uuid)
                    : (validNameChecker.check(name) ? name : uuid.toString());
        } else {
            resolvedName = StringUtils.left(uuidOrName, 16);

            uuid = platformServerAdapter.isOnlineMode()
                    ? profileResolver.resolveOnlineUUID(uuidOrName)
                    : profileResolver.resolveOfflineUUID(uuidOrName);

            // use offline uuid if empty
            if (uuid == null) {
                uuid = profileResolver.resolveOfflineUUID(uuidOrName);
            }
        }

        // just save uuid name string for update in feature
        if (StringUtils.isEmpty(resolvedName) || !validNameChecker.check(resolvedName)) {
            resolvedName = uuid.toString();
        }

        // get player ip
        String playerIp = platformPlayerAdapter.getIp(fTarget);

        // is player online?
        boolean isOnline = platformPlayerAdapter.isOnline(fTarget);

        // save to database
        fPlayerService.saveOrUpdate(uuid, StringUtils.left(resolvedName, 16), playerIp, isOnline);

        // invalidate cached unknown player
        fPlayerService.invalidateOfflineCache(uuid, false);

        return fPlayerService.getFPlayer(uuid);
    }

    public void kickPlayer(FEntity fModerator, FPlayer fTarget) {
        if (!platformPlayerAdapter.isOnline(fTarget)) return;
        if (moduleController.isDisabledFor(this, fModerator)) return;
        if (permissionChecker.check(fTarget, permission().bypass())) return;
        if (isWhitelisted(fTarget)) return;

        platformPlayerAdapter.kick(fTarget, messagePipeline.build(MessageContext.builder()
                .sender(fModerator)
                .receiver(fTarget)
                .message(localization(fTarget).person())
                .build()
        ));
    }

    public enum Action {
        ADD,
        LIST,
        OFF,
        ON,
        IMPORT,
        REMOVE
    }

    private record MinecraftWhitelistEntry(
            UUID uuid,
            String name
    ){}

    private record HytaleWhitelist(
            boolean enabled,
            List<String> list
    ){}

}
