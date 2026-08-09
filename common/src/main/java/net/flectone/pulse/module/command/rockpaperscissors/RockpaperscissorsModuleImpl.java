package net.flectone.pulse.module.command.rockpaperscissors;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.rockpaperscissors.listener.RockpaperscissorsProxyMessageListener;
import net.flectone.pulse.module.command.rockpaperscissors.model.RockPaperScissors;
import net.flectone.pulse.module.command.rockpaperscissors.model.RockPaperScissorsMessageContext;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.IgnoreSender;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.UUIDParser;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RockpaperscissorsModuleImpl implements RockpaperscissorsModule {

    private final Map<UUID, RockPaperScissors> gameMap = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final ProxySender proxySender;
    private final FPlayerService fPlayerService;
    private final CommandParserProvider commandParserProvider;
    private final SocialService socialService;
    private final IgnoreSender ignoreSender;
    private final DisableSender disableSender;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final ProxyRegistry proxyRegistry;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        String promptPlayer = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::player);
        String promptMove = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::move);
        String promptUUID = commandModuleController.addPrompt(this, 2, Localization.Command.Prompt::id);
        commandModuleController.registerCommand(this, manager -> manager
                .permission(permission().name())
                .required(promptPlayer, commandParserProvider.playerParser())
                .optional(promptMove, commandParserProvider.nativeSingleMessageParser())
                .optional(promptUUID, UUIDParser.uuidParser())
        );

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(RockpaperscissorsProxyMessageListener.class);
        }
    }

    @Override
    public void onDisable() {
        gameMap.clear();
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String player = commandModuleController.getArgument(this, commandContext, 0);
        FPlayer fReceiver = fPlayerService.getFPlayer(player);
        if (!fReceiver.isOnline() || !socialService.canSeeVanished(fReceiver, fPlayer)) {
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

        if (fReceiver.equals(fPlayer)) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).myself())
                            .build()
                    )
                    .build()
            );

            return;
        }

        if (ignoreSender.sendIfIgnored(fPlayer, fReceiver)) return;
        if (disableSender.sendIfDisabled(fPlayer, fReceiver, name())) return;

        String promptMove = commandModuleController.getPrompt(this, 1);
        Optional<String> optionalMove = commandContext.optional(promptMove);
        String move = optionalMove.orElse(null);

        String promptUUID = commandModuleController.getPrompt(this, 2);
        Optional<UUID> optionalUUID = commandContext.optional(promptUUID);
        UUID uuid = optionalUUID.orElse(null);

        if (move != null && uuid != null) {
            finalMove(fPlayer, fReceiver, move, uuid);
            return;
        }

        RockPaperScissors rockPaperScissors = new RockPaperScissors(fPlayer.uuid(), fReceiver.uuid());

        proxySender.send(fPlayer, name(), dataOutputStream -> {
            dataOutputStream.writeUTF(GamePhase.CREATE.name());
            dataOutputStream.writeUTF(rockPaperScissors.getId().toString());
            dataOutputStream.writeUTF(rockPaperScissors.getReceiver().toString());
        }, UUID.randomUUID());

        create(rockPaperScissors.getId(), fPlayer, fReceiver.uuid());

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .sound(soundOrThrow())
                .messageContext(fResolver -> RockPaperScissorsMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fPlayer)
                                .receiver(fResolver)
                                .message(StringUtils.replaceEach(localization(fResolver).formatMove(),
                                        new String[]{"<target>", "<uuid>"},
                                        new String[]{fReceiver.name(), rockPaperScissors.getId().toString()}
                                ))
                                .build()
                        )
                        .rockPaperScissors(rockPaperScissors)
                        .gamePhase(GamePhase.CREATE)
                        .build()
                )
                .build()
        );
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_ROCKPAPERSCISSORS;
    }

    @Override
    public Command.Rockpaperscissors config() {
        return fileFacade.command().rockpaperscissors();
    }

    @Override
    public Permission.Command.Rockpaperscissors permission() {
        return fileFacade.permission().command().rockpaperscissors();
    }

    @Override
    public Localization.Command.Rockpaperscissors localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().rockpaperscissors();
    }

    @Override
    public void finalMove(FPlayer fPlayer, FPlayer fReceiver, String move, UUID uuid) {
        List<String> strategy = config().strategies().get(move);

        if (strategy == null) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).wrongMove())
                            .build()
                    )
                    .build()
            );

            return;
        }

        RockPaperScissors rockPaperScissors = gameMap.get(uuid);

        if (rockPaperScissors == null) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).nullGame())
                            .build()
                    )
                    .build()
            );

            return;
        }

        if (rockPaperScissors.getSenderMove() != null) {
            if (rockPaperScissors.getSender().equals(fPlayer.uuid())) {
                messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                        .messageContext(fResolver -> MessageContext.builder()
                                .sender(fPlayer)
                                .receiver(fResolver)
                                .message(localization(fResolver).already())
                                .build()
                        )
                        .build()
                );

                return;
            }

            boolean isSent = proxySender.send(fPlayer, name(), dataOutputStream -> {
                dataOutputStream.writeUTF(GamePhase.END.name());
                dataOutputStream.writeUTF(rockPaperScissors.getId().toString());
                dataOutputStream.writeUTF(move);
            }, UUID.randomUUID());

            if (isSent) return;

            end(rockPaperScissors.getId(), fPlayer, move, UUID.randomUUID());

            return;
        }

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .messageContext(fResolver -> MessageContext.builder()
                        .sender(fPlayer)
                        .receiver(fResolver)
                        .message(localization(fResolver).sender())
                        .tagResolver(messagePipeline.targetTag(fResolver, fReceiver))
                        .build()
                )
                .build()
        );

        boolean isSent = proxySender.send(fPlayer, name(), dataOutputStream -> {
            dataOutputStream.writeUTF(GamePhase.MOVE.name());
            dataOutputStream.writeUTF(rockPaperScissors.getId().toString());
            dataOutputStream.writeUTF(move);
        }, UUID.randomUUID());

        if (isSent) return;

        move(rockPaperScissors.getId(), fPlayer, move, UUID.randomUUID());
    }

    @Override
    public void end(UUID id, FPlayer fPlayer, String move, UUID metadataUUID) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        RockPaperScissors rockPaperScissors = gameMap.get(id);
        if (rockPaperScissors == null) return;

        FPlayer fReceiver = fPlayerService.getFPlayer(rockPaperScissors.getSender());

        gameMap.remove(id);

        String senderMove = rockPaperScissors.getSenderMove();

        boolean isDraw = senderMove.equalsIgnoreCase(move);

        if (isDraw) {
            Function<FPlayer, String> message = fResolver -> Strings.CS.replace(
                    localization(fResolver).formatDraw(),
                    "<move>",
                    localization(fResolver).strategies().get(move)
            );

            messageDispatcher.dispatch(this, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .uuid(metadataUUID)
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(message.apply(fResolver))
                            .build()
                    )
                    .build()
            );

            messageDispatcher.dispatch(this, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .uuid(metadataUUID)
                            .sender(fReceiver)
                            .receiver(fResolver)
                            .message(message.apply(fResolver))
                            .build()
                    )
                    .build()
            );

            return;
        }

        FEntity winFPlayer = config().strategies().get(move).contains(senderMove) ? fPlayer : fReceiver;

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .filter(List.of(fPlayer, fReceiver))
                .messageContext(fResolver -> RockPaperScissorsMessageContext.builder()
                        .base(MessageContext.builder()
                                .uuid(metadataUUID)
                                .sender(winFPlayer)
                                .receiver(fResolver)
                                .message(StringUtils.replaceEach(
                                        localization(fResolver).formatWin(),
                                        new String[]{"<sender_move>", "<receiver_move>"},
                                        new String[]{localization(fResolver).strategies().get(senderMove), localization(fResolver).strategies().get(move)}
                                ))
                                .build()
                        )
                        .rockPaperScissors(rockPaperScissors)
                        .gamePhase(GamePhase.END)
                        .build()
                )
                .build()
        );
    }

    @Override
    public void move(UUID id, FEntity fPlayer, String move, UUID metadataUUID) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        RockPaperScissors rockPaperScissors = gameMap.get(id);
        if (rockPaperScissors == null) return;

        FPlayer fReceiver = fPlayerService.getFPlayer(rockPaperScissors.getReceiver());

        rockPaperScissors.setSenderMove(move);

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .filter(fReceiver)
                .messageContext(fResolver -> RockPaperScissorsMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fPlayer)
                                .receiver(fResolver)
                                .flag(MessageFlag.COLOR_CONTEXT_SENDER, false)
                                .message(localization(fResolver).receiver())
                                .build()
                        )
                        .rockPaperScissors(rockPaperScissors)
                        .gamePhase(GamePhase.MOVE)
                        .build()
                )
                .build()
        );

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .filter(fReceiver)
                .messageContext(fResolver -> RockPaperScissorsMessageContext.builder()
                        .base(MessageContext.builder()
                                .uuid(metadataUUID)
                                .sender(fPlayer)
                                .receiver(fResolver)
                                .flag(MessageFlag.COLOR_CONTEXT_SENDER, false)
                                .message(StringUtils.replaceEach(
                                        localization(fResolver).formatMove(),
                                        new String[]{"<target>", "<uuid>"},
                                        new String[]{fPlayer.name(), rockPaperScissors.getId().toString()}
                                ))
                                .build()
                        )
                        .rockPaperScissors(rockPaperScissors)
                        .gamePhase(GamePhase.MOVE)
                        .build()
                )
                .build()
        );
    }

    @Override
    public void create(UUID id, FEntity fPlayer, UUID receiver) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        RockPaperScissors rockPaperScissors = new RockPaperScissors(id, fPlayer.uuid(), receiver);
        gameMap.put(id, rockPaperScissors);
    }

}
