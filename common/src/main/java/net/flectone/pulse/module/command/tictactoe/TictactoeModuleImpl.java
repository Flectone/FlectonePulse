package net.flectone.pulse.module.command.tictactoe;

import com.google.gson.Gson;
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
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.tictactoe.listener.TictactoeProxyMessageListener;
import net.flectone.pulse.module.command.tictactoe.model.TicTacToe;
import net.flectone.pulse.module.command.tictactoe.model.TicTacToeMessageContext;
import net.flectone.pulse.module.command.tictactoe.service.TictactoeService;
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
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TictactoeModuleImpl implements TictactoeModule {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final TictactoeService tictactoeService;
    private final ProxySender proxySender;
    private final SocialService socialService;
    private final CommandParserProvider commandParserProvider;
    private final Gson gson;
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
        String promptHard = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::hard);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .required(promptPlayer, commandParserProvider.playerParser())
                .optional(promptHard, commandParserProvider.booleanParser())
                .permission(permission().name())
        );

        String promptId = commandModuleController.addPrompt(this, 2, Localization.Command.Prompt::id);
        String promptMove = commandModuleController.addPrompt(this, 3, Localization.Command.Prompt::move);
        commandModuleController.registerSubCommand(this, config().subCommandMove(), commandBuilder -> commandBuilder
                .required(promptId, commandParserProvider.integerParser())
                .required(promptMove, commandParserProvider.singleMessageParser())
                .permission(permission().name())
                .handler(commandContext -> executeMove(commandContext.sender(), commandContext))
        );

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(TictactoeProxyMessageListener.class);
        }
    }

    @Override
    public void onDisable() {
        tictactoeService.clear();
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String receiverName = commandModuleController.getArgument(this, commandContext, 0);
        String promptHard = commandModuleController.getPrompt(this, 1);

        Optional<Boolean> optionalBoolean = commandContext.optional(promptHard);
        boolean isHard = optionalBoolean.orElse(true);

        FPlayer fReceiver = fPlayerService.getFPlayer(receiverName);
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

        TicTacToe ticTacToe = tictactoeService.create(fPlayer, fReceiver, isHard);

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .sound(soundOrThrow())
                .messageContext(fResolver -> TicTacToeMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fPlayer)
                                .receiver(fResolver)
                                .message(localization(fResolver).sender())
                                .tagResolver(messagePipeline.targetTag(fResolver, fReceiver))
                                .build()
                        )
                        .ticTacToe(ticTacToe)
                        .gamePhase(GamePhase.CREATE)
                        .build()
                )
                .build()
        );

        UUID metadataUUID = UUID.randomUUID();
        boolean isSent = proxySender.send(fPlayer, name(), dataOutputStream -> {
            dataOutputStream.writeUTF(GamePhase.CREATE.name());
            dataOutputStream.writeUTF(gson.toJson(fReceiver));
            dataOutputStream.writeInt(ticTacToe.getId());
            dataOutputStream.writeBoolean(isHard);
        }, metadataUUID);

        if (isSent) return;

        sendCreateMessage(fPlayer, fReceiver, ticTacToe, metadataUUID);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_TICTACTOE;
    }

    @Override
    public Command.Tictactoe config() {
        return fileFacade.command().tictactoe();
    }

    @Override
    public Permission.Command.Tictactoe permission() {
        return fileFacade.permission().command().tictactoe();
    }

    @Override
    public Localization.Command.Tictactoe localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().tictactoe();
    }

    // /tictactoe %d create
    @Override
    public void sendCreateMessage(FPlayer fPlayer, FPlayer fReceiver, TicTacToe ticTacToe, UUID metadataUUID) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;
        if (!socialService.canSeeVanished(fPlayer, fReceiver)
                || !socialService.canSeeVanished(fReceiver, fPlayer)) return;

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .filter(fReceiver)
                .sound(soundOrThrow())
                .messageContext(fResolver -> TicTacToeMessageContext.builder()
                        .base(MessageContext.builder()
                                .uuid(metadataUUID)
                                .sender(fPlayer)
                                .receiver(fResolver)
                                .flag(MessageFlag.COLOR_CONTEXT_SENDER, false)
                                .message(Strings.CS.replace(String.format(localization(fResolver).receiver(), ticTacToe.getId()), "<command>", commandModuleController.getCommandName(this) + config().subCommandMove()))
                                .build()
                        )
                        .ticTacToe(ticTacToe)
                        .gamePhase(GamePhase.CREATE)
                        .build()
                )
                .build()
        );
    }

    // /tictactoe %d <move>
    @Override
    public void sendMoveMessage(FPlayer fPlayer, FPlayer fReceiver, TicTacToe ticTacToe, int typeTitle, String move, UUID metadataUUID) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;
        if (!socialService.canSeeVanished(fPlayer, fReceiver)
                || !socialService.canSeeVanished(fReceiver, fPlayer)) return;
        if (ticTacToe == null) return;

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .messageContext(fResolver -> TicTacToeMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fPlayer)
                                .receiver(fResolver)
                                .message(getMoveMessage(fReceiver, fResolver, ticTacToe, typeTitle, move))
                                .tagResolver(messagePipeline.targetTag(fResolver, fReceiver))
                                .build()
                        )
                        .ticTacToe(ticTacToe)
                        .gamePhase(GamePhase.MOVE)
                        .build()
                )
                .build()
        );

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .filter(fReceiver)
                .messageContext(fResolver -> TicTacToeMessageContext.builder()
                        .base(MessageContext.builder()
                                .uuid(metadataUUID)
                                .sender(fPlayer)
                                .receiver(fResolver)
                                .flag(MessageFlag.COLOR_CONTEXT_SENDER, false)
                                .message(getMoveMessage(fReceiver, fResolver, ticTacToe, typeTitle, move))
                                .tagResolver(messagePipeline.targetTag(fResolver, fReceiver))
                                .build()
                        )
                        .ticTacToe(ticTacToe)
                        .gamePhase(GamePhase.MOVE)
                        .build()
                )
                .build()
        );
    }

    @Override
    public void executeMove(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        int tictactoeID = commandModuleController.getArgument(this, commandContext, 2);
        String move = commandModuleController.getArgument(this, commandContext, 3);

        FPlayer finalFPlayer = fPlayer;

        TicTacToe ticTacToe = tictactoeService.get(tictactoeID);
        if (ticTacToe == null || ticTacToe.isEnded() || !ticTacToe.contains(fPlayer) || (move.equals("create") && ticTacToe.isCreated())) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(finalFPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).wrongGame())
                            .build()
                    )
                    .build()
            );

            return;
        }

        if (!ticTacToe.move(fPlayer, move)) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(finalFPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).wrongMove())
                            .build()
                    )
                    .build()
            );

            return;
        }

        FPlayer fReceiver = fPlayerService.getFPlayer(ticTacToe.getNextPlayer());
        if (!fReceiver.isOnline() || !socialService.canSeeVanished(fReceiver, fPlayer)) {
            ticTacToe.setEnded(true);
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(finalFPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).wrongByPlayer())
                            .build()
                    )
                    .build()
            );

            return;
        }

        int typeTitle;
        if (ticTacToe.isWin()) {
            ticTacToe.setEnded(true);
            typeTitle = 1;

            // swap FPlayers
            FPlayer tempFPlayer = fPlayer;
            fPlayer = fReceiver;
            fReceiver = tempFPlayer;
        } else if (ticTacToe.isDraw()) {
            ticTacToe.setEnded(true);
            typeTitle = -1;

            // swap FPlayers
            FPlayer tempFPlayer = fPlayer;
            fPlayer = fReceiver;
            fReceiver = tempFPlayer;
        } else {
            typeTitle = 0;
        }

        FPlayer finalFReceiver = fReceiver;
        UUID metadataUUID = UUID.randomUUID();
        boolean isSent = proxySender.send(fPlayer, name(), dataOutputStream -> {
            dataOutputStream.writeUTF(GamePhase.MOVE.name());
            dataOutputStream.writeUTF(gson.toJson(finalFReceiver));
            dataOutputStream.writeUTF(ticTacToe.toString());
            dataOutputStream.writeInt(typeTitle);
            dataOutputStream.writeUTF(move);
        }, metadataUUID);

        if (isSent) return;

        sendMoveMessage(fPlayer, finalFReceiver, ticTacToe, typeTitle, move, metadataUUID);
    }

    @Override
    public String getMoveMessage(FPlayer fPlayer, FPlayer fResolver, TicTacToe ticTacToe, int typeTile, String move) {
        Localization.Command.Tictactoe localization = localization(fResolver);

        String title = (switch (typeTile) {
            case 1 -> localization.formatWin();
            case -1 -> localization.formatDraw();
            default -> localization.formatMove();
        });

        Localization.Command.Tictactoe.Symbol messageSymbol = localization.symbol();

        String symbolFirst = messageSymbol.first();
        String symbolSecond = messageSymbol.second();

        String formatField = StringUtils.replaceEach(
                String.join("<br>", localization.field()),
                new String[]{"<current_move>", "<last_move>"},
                new String[]{
                        ticTacToe.isEnded() ? "" : localization.currentMove(),
                        localization.lastMove()
                }
        );

        formatField = StringUtils.replaceEach(
                formatField,
                new String[]{"<title>", "<symbol>", "<move>"},
                new String[]{
                        title,
                        ticTacToe.getFirstPlayer() == fPlayer.id() ? symbolFirst : symbolSecond,
                        move
                }
        );

        String symbolEmpty = Strings.CS.replace(String.format(messageSymbol.blank(), ticTacToe.getId()), "<command>", commandModuleController.getCommandName(this) + config().subCommandMove());
        String symbolFirstRemove = messageSymbol.firstRemove();
        String symbolFirstWin = messageSymbol.firstWin();
        String symbolSecondRemove = messageSymbol.secondRemove();
        String symbolSecondWin = messageSymbol.secondWin();

        return build(ticTacToe,
                formatField,
                symbolFirst,
                symbolFirstRemove,
                symbolFirstWin,
                symbolSecond,
                symbolSecondRemove,
                symbolSecondWin,
                symbolEmpty
        );
    }

    public String build(TicTacToe ticTacToe,
                        String formatField,
                        String first,
                        String firstRemove,
                        String firstWin,
                        String second,
                        String secondRemove,
                        String secondWin,
                        String empty) {

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                formatField = RegExUtils.replaceFirst(formatField, "\\[#]",
                        switch (ticTacToe.getField()[i][j]) {
                            case TicTacToe.FIRST_VALUE -> first;
                            case TicTacToe.FIRST_VALUE * TicTacToe.REMOVE_MULTIPLIER -> firstRemove;
                            case TicTacToe.FIRST_VALUE + TicTacToe.WIN_OFFSET -> firstWin;
                            case TicTacToe.SECOND_VALUE -> second;
                            case TicTacToe.SECOND_VALUE * TicTacToe.REMOVE_MULTIPLIER -> secondRemove;
                            case TicTacToe.SECOND_VALUE + TicTacToe.WIN_OFFSET -> secondWin;
                            default -> empty.replace("<move>", i + "-" + j);
                        }
                );
            }
        }

        return formatField;
    }

}
