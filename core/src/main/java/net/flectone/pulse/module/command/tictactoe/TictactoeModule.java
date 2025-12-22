package net.flectone.pulse.module.command.tictactoe;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.tictactoe.service.TictactoeService;
import net.flectone.pulse.module.command.tictactoe.model.TicTacToe;
import net.flectone.pulse.module.command.tictactoe.model.TicTacToeMetadata;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.IgnoreSender;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.context.CommandContext;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TictactoeModule extends AbstractModuleCommand<Localization.Command.Tictactoe> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final TictactoeService tictactoeService;
    private final ProxySender proxySender;
    private final IntegrationModule integrationModule;
    private final CommandParserProvider commandParserProvider;
    private final Gson gson;
    private final IgnoreSender ignoreSender;
    private final DisableSender disableSender;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::player);
        String promptHard = addPrompt(1, Localization.Command.Prompt::hard);
        registerCommand(manager -> manager
               .required(promptPlayer, commandParserProvider.playerParser())
               .optional(promptHard, commandParserProvider.booleanParser())
               .permission(permission().name())
        );

        String promptId = addPrompt(2, Localization.Command.Prompt::id);
        String promptMove = addPrompt(3, Localization.Command.Prompt::move);
        registerCustomCommand(manager ->
                manager.commandBuilder(getCommandName() + "move")
                        .required(promptId, commandParserProvider.integerParser())
                        .required(promptMove, commandParserProvider.singleMessageParser())
                        .permission(permission().name())
                        .handler(commandContext -> executeMove(commandContext.sender(), commandContext))
        );
    }

    @Override
    public void onDisable() {
        super.onDisable();

        tictactoeService.clear();
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String receiverName = getArgument(commandContext, 0);
        String promptHard = getPrompt(1);

        Optional<Boolean> optionalBoolean = commandContext.optional(promptHard);
        boolean isHard = optionalBoolean.orElse(true);

        FPlayer fReceiver = fPlayerService.getFPlayer(receiverName);
        if (!fReceiver.isOnline() || !integrationModule.canSeeVanished(fReceiver, fPlayer)) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Tictactoe::nullPlayer)
                    .build()
            );

            return;
        }

        if (fReceiver.equals(fPlayer)) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Tictactoe::myself)
                    .build()
            );

            return;
        }

        fPlayerService.loadIgnoresIfOffline(fReceiver);
        if (ignoreSender.sendIfIgnored(fPlayer, fReceiver)) return;

        fPlayerService.loadSettingsIfOffline(fReceiver);
        if (disableSender.sendIfDisabled(fPlayer, fReceiver, messageType())) return;

        TicTacToe ticTacToe = tictactoeService.create(fPlayer, fReceiver, isHard);

        sendMessage(TicTacToeMetadata.<Localization.Command.Tictactoe>builder()
                .sender(fPlayer)
                .format(Localization.Command.Tictactoe::sender)
                .ticTacToe(ticTacToe)
                .gamePhase(GamePhase.CREATE)
                .sound(soundOrThrow())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, fReceiver)})
                .build()
        );

        UUID metadataUUID = UUID.randomUUID();
        boolean isSent = proxySender.send(fPlayer, MessageType.COMMAND_TICTACTOE, dataOutputStream -> {
            dataOutputStream.writeUTF(GamePhase.CREATE.name());
            dataOutputStream.writeUTF(gson.toJson(fReceiver));
            dataOutputStream.writeInt(ticTacToe.getId());
            dataOutputStream.writeBoolean(isHard);
        }, metadataUUID);

        if (isSent) return;

        sendCreateMessage(fPlayer, fReceiver, ticTacToe, metadataUUID);
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_TICTACTOE;
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
    public Localization.Command.Tictactoe localization(FEntity sender) {
        return fileFacade.localization(sender).command().tictactoe();
    }

    // /tictactoe %d create
    public void sendCreateMessage(FPlayer fPlayer, FPlayer fReceiver, TicTacToe ticTacToe, UUID metadataUUID) {
        if (isModuleDisabledFor(fPlayer)) return;
        if (!integrationModule.canSeeVanished(fPlayer, fReceiver)
                || !integrationModule.canSeeVanished(fReceiver, fPlayer)) return;

        sendMessage(TicTacToeMetadata.<Localization.Command.Tictactoe>builder()
                .uuid(metadataUUID)
                .sender(fPlayer)
                .filterPlayer(fReceiver, false)
                .format(message -> String.format(message.receiver(), ticTacToe.getId()))
                .ticTacToe(ticTacToe)
                .gamePhase(GamePhase.CREATE)
                .sound(soundOrThrow())
                .build()
        );
    }

    // /tictactoe %d <move>
    public void sendMoveMessage(FPlayer fPlayer, FPlayer fReceiver, TicTacToe ticTacToe, int typeTitle, String move, UUID metadataUUID) {
        if (isModuleDisabledFor(fPlayer)) return;
        if (!integrationModule.canSeeVanished(fPlayer, fReceiver)
                || !integrationModule.canSeeVanished(fReceiver, fPlayer)) return;
        if (ticTacToe == null) return;

        sendMessage(TicTacToeMetadata.<Localization.Command.Tictactoe>builder()
                .sender(fPlayer)
                .format(getMoveMessage(ticTacToe, fReceiver, typeTitle, move))
                .ticTacToe(ticTacToe)
                .gamePhase(GamePhase.MOVE)
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, fReceiver)})
                .build()
        );

        sendMessage(TicTacToeMetadata.<Localization.Command.Tictactoe>builder()
                .uuid(metadataUUID)
                .sender(fPlayer)
                .filterPlayer(fReceiver, false)
                .format(getMoveMessage(ticTacToe, fReceiver, typeTitle, move))
                .ticTacToe(ticTacToe)
                .gamePhase(GamePhase.MOVE)
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, fReceiver)})
                .build()
        );
    }

    public void executeMove(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        int tictactoeID = getArgument(commandContext, 2);
        String move = getArgument(commandContext, 3);

        TicTacToe ticTacToe = tictactoeService.get(tictactoeID);
        if (ticTacToe == null || ticTacToe.isEnded() || !ticTacToe.contains(fPlayer) || (move.equals("create") && ticTacToe.isCreated())) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Tictactoe::wrongGame)
                    .build()
            );

            return;
        }

        if (!ticTacToe.move(fPlayer, move)) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Tictactoe::wrongMove)
                    .build()
            );

            return;
        }

        FPlayer fReceiver = fPlayerService.getFPlayer(ticTacToe.getNextPlayer());
        if (!fReceiver.isOnline() || !integrationModule.canSeeVanished(fReceiver, fPlayer)) {
            ticTacToe.setEnded(true);
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Tictactoe::wrongByPlayer)
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
        boolean isSent = proxySender.send(fPlayer, MessageType.COMMAND_TICTACTOE, dataOutputStream -> {
            dataOutputStream.writeUTF(GamePhase.MOVE.name());
            dataOutputStream.writeUTF(gson.toJson(finalFReceiver));
            dataOutputStream.writeUTF(ticTacToe.toString());
            dataOutputStream.writeInt(typeTitle);
            dataOutputStream.writeUTF(move);
        }, metadataUUID);

        if (isSent) return;

        sendMoveMessage(fPlayer, finalFReceiver, ticTacToe, typeTitle, move, metadataUUID);
    }

    public BiFunction<FPlayer, Localization.Command.Tictactoe, String> getMoveMessage(TicTacToe ticTacToe,
                                                                                      FPlayer fPlayer,
                                                                                      int typeTile,
                                                                                      String move) {
        return (fResolver, message) -> {
            String title = (switch (typeTile) {
                case 1 -> message.formatWin();
                case -1 -> message.formatDraw();
                default -> message.formatMove();
            });

            Localization.Command.Tictactoe.Symbol messageSymbol = message.symbol();

            String symbolFirst = messageSymbol.first();
            String symbolSecond = messageSymbol.second();

            String formatField = StringUtils.replaceEach(
                    String.join("<br>", message.field()),
                    new String[]{"<current_move>", "<last_move>"},
                    new String[]{
                            ticTacToe.isEnded() ? "" : message.currentMove(),
                            message.lastMove()
                    }
            );

           formatField = StringUtils.replaceEach(
                    formatField,
                    new String[]{"<title>", "<symbol>", "<move>"},
                    new String[]{
                            title,
                            ticTacToe.getFirstPlayer() == fPlayer.getId() ? symbolFirst : symbolSecond,
                            move
                    }
            );

            String symbolEmpty = messageSymbol.blank();
            String symbolFirstRemove = messageSymbol.firstRemove();
            String symbolFirstWin = messageSymbol.firstWin();
            String symbolSecondRemove = messageSymbol.secondRemove();
            String symbolSecondWin = messageSymbol.secondWin();

            return ticTacToe.build(
                    formatField,
                    symbolFirst,
                    symbolFirstRemove,
                    symbolFirstWin,
                    symbolSecond,
                    symbolSecondRemove,
                    symbolSecondWin,
                    String.format(symbolEmpty, ticTacToe.getId())
            );
        };
    }

    public enum GamePhase {
        CREATE,
        MOVE
    }
}
