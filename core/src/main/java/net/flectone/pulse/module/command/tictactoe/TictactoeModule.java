package net.flectone.pulse.module.command.tictactoe;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.tictactoe.manager.TictactoeManager;
import net.flectone.pulse.module.command.tictactoe.model.TicTacToe;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.Optional;
import java.util.function.BiFunction;

@Singleton
public class TictactoeModule extends AbstractModuleCommand<Localization.Command.Tictactoe> {

    private final Command.Tictactoe command;
    private final Permission.Command.Tictactoe permission;
    private final FPlayerService fPlayerService;
    private final TictactoeManager tictactoeManager;
    private final ProxySender proxySender;
    private final IntegrationModule integrationModule;
    private final CommandParserProvider commandParserProvider;
    private final Gson gson;

    @Inject
    public TictactoeModule(FileResolver fileResolver,
                           FPlayerService fPlayerService,
                           TictactoeManager tictactoeManager,
                           ProxySender proxySender,
                           IntegrationModule integrationModule,
                           CommandParserProvider commandParserProvider,
                           Gson gson) {
        super(localization -> localization.getCommand().getTictactoe(), Command::getTictactoe, fPlayer -> fPlayer.isSetting(FPlayer.Setting.TICTACTOE));

        this.command = fileResolver.getCommand().getTictactoe();
        this.permission = fileResolver.getPermission().getCommand().getTictactoe();
        this.fPlayerService = fPlayerService;
        this.tictactoeManager = tictactoeManager;
        this.proxySender = proxySender;
        this.integrationModule = integrationModule;
        this.commandParserProvider = commandParserProvider;
        this.gson = gson;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        String promptHard = addPrompt(1, Localization.Command.Prompt::getHard);
        registerCommand(manager -> manager
               .required(promptPlayer, commandParserProvider.playerParser())
               .optional(promptHard, commandParserProvider.booleanParser())
               .permission(permission.getName())
        );

        String promptId = addPrompt(2, Localization.Command.Prompt::getId);
        String promptMove = addPrompt(3, Localization.Command.Prompt::getMove);
        registerCustomCommand(manager ->
                manager.commandBuilder(getCommandName() + "move")
                        .required(promptId, commandParserProvider.integerParser())
                        .required(promptMove, commandParserProvider.singleMessageParser())
                        .permission(permission.getName())
                        .handler(commandContext -> executeMove(commandContext.sender(), commandContext))
        );
    }

    @Override
    public void onDisable() {
        super.onDisable();

        tictactoeManager.clear();
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableSource.YOU)) return;
        if (checkMute(fPlayer)) return;

        String receiverName = getArgument(commandContext, 0);
        String promptHard = getPrompt(1);

        Optional<Boolean> optionalBoolean = commandContext.optional(promptHard);
        boolean isHard = optionalBoolean.orElse(true);

        FPlayer fReceiver = fPlayerService.getFPlayer(receiverName);
        if (!fReceiver.isOnline() || !integrationModule.canSeeVanished(fReceiver, fPlayer)) {
            builder(fPlayer)
                    .format(Localization.Command.Tictactoe::getNullPlayer)
                    .sendBuilt();
            return;
        }

        if (fReceiver.equals(fPlayer)) {
            builder(fPlayer)
                    .format(Localization.Command.Tictactoe::getMyself)
                    .sendBuilt();
            return;
        }

        fPlayerService.loadIgnores(fPlayer);

        if (checkIgnore(fPlayer, fReceiver)) return;
        if (checkDisable(fPlayer, fReceiver, DisableSource.HE)) return;

        TicTacToe ticTacToe = tictactoeManager.create(fPlayer, fReceiver, isHard);

        builder(fReceiver)
                .receiver(fPlayer)
                .format((fResolver, s) -> s.getSender())
                .sound(getSound())
                .sendBuilt();

        boolean isSent = proxySender.send(fPlayer, MessageType.COMMAND_TICTACTOE_CREATE, dataOutputStream -> {
            dataOutputStream.writeUTF(gson.toJson(fReceiver));
            dataOutputStream.writeInt(ticTacToe.getId());
            dataOutputStream.writeBoolean(isHard);
        });

        if (isSent) return;

        sendCreateMessage(fPlayer, fReceiver, ticTacToe);
    }

    // /tictactoe %d create
    public void sendCreateMessage(FPlayer fPlayer, FPlayer fReceiver, TicTacToe ticTacToe) {
        if (isModuleDisabledFor(fPlayer)) return;
        if (!integrationModule.canSeeVanished(fPlayer, fReceiver)
                || !integrationModule.canSeeVanished(fReceiver, fPlayer)) return;

        builder(fPlayer)
                .receiver(fReceiver)
                .format(message -> String.format(message.getReceiver(), ticTacToe.getId()))
                .sound(getSound())
                .sendBuilt();
    }

    // /tictactoe %d <move>
    public void sendMoveMessage(FPlayer fPlayer, FPlayer fReceiver, TicTacToe ticTacToe, int typeTitle, String move) {
        if (isModuleDisabledFor(fPlayer)) return;
        if (!integrationModule.canSeeVanished(fPlayer, fReceiver)
                || !integrationModule.canSeeVanished(fReceiver, fPlayer)) return;
        if (ticTacToe == null) return;

        builder(fPlayer)
                .receiver(fReceiver)
                .format(getMoveMessage(ticTacToe, fReceiver, fPlayer, typeTitle, move))
                .sendBuilt();
    }

    public void executeMove(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        int tictactoeID = getArgument(commandContext, 2);
        String move = getArgument(commandContext, 3);

        TicTacToe ticTacToe = tictactoeManager.get(tictactoeID);
        if (ticTacToe == null || ticTacToe.isEnded() || !ticTacToe.contains(fPlayer) || (move.equals("create") && ticTacToe.isCreated())) {
            builder(fPlayer)
                    .format(Localization.Command.Tictactoe::getWrongGame)
                    .sendBuilt();
            return;
        }

        if (!ticTacToe.move(fPlayer, move)) {
            builder(fPlayer)
                    .format(Localization.Command.Tictactoe::getWrongMove)
                    .sendBuilt();
            return;
        }

        FPlayer fReceiver = fPlayerService.getFPlayer(ticTacToe.getNextPlayer());
        if (!fReceiver.isOnline() || !integrationModule.canSeeVanished(fReceiver, fPlayer)) {
            ticTacToe.setEnded(true);
            builder(fPlayer)
                    .format(Localization.Command.Tictactoe::getWrongByPlayer)
                    .sendBuilt();
            return;
        }

        int typeTitle = 0;

        if (ticTacToe.isWin()) {
            ticTacToe.setEnded(true);
            typeTitle = 1;
        }

        if (ticTacToe.isDraw()) {
            ticTacToe.setEnded(true);
            typeTitle = -1;
        }

        int finalTypeTitle = typeTitle;

        builder(fReceiver)
                .receiver(fPlayer)
                .format(getMoveMessage(ticTacToe, fReceiver, fPlayer, finalTypeTitle, move))
                .sendBuilt();

        boolean isSent = proxySender.send(fPlayer, MessageType.COMMAND_TICTACTOE_MOVE, dataOutputStream -> {
            dataOutputStream.writeUTF(gson.toJson(fReceiver));
            dataOutputStream.writeUTF(ticTacToe.toString());
            dataOutputStream.writeInt(finalTypeTitle);
            dataOutputStream.writeUTF(move);
        });

        if (isSent) return;

        builder(fPlayer)
                .receiver(fReceiver)
                .format(getMoveMessage(ticTacToe, fReceiver, fPlayer, finalTypeTitle, move))
                .sendBuilt();
    }

    public BiFunction<FPlayer, Localization.Command.Tictactoe, String> getMoveMessage(TicTacToe ticTacToe,
                                                                                      FPlayer fPlayer,
                                                                                      FPlayer fReceiver,
                                                                                      int typeTile,
                                                                                      String move) {
        return (fResolver, message) -> {
            String title = (switch (typeTile) {
                case 1 -> Strings.CS.replace(message.getFormatWin(), "<player>", fReceiver.getName());
                case -1 -> message.getFormatDraw();
                default -> Strings.CS.replace(message.getFormatMove(), "<player>", fPlayer.getName());
            });

            Localization.Command.Tictactoe.Symbol messageSymbol = message.getSymbol();

            String symbolFirst = messageSymbol.getFirst();
            String symbolSecond = messageSymbol.getSecond();

            String formatField = StringUtils.replaceEach(
                    String.join("<br>", message.getField()),
                    new String[]{"<current_move>", "<last_move>"},
                    new String[]{
                            ticTacToe.isEnded() ? "" : message.getCurrentMove(),
                            message.getLastMove()
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

            String symbolEmpty = messageSymbol.getBlank();
            String symbolFirstRemove = messageSymbol.getFirstRemove();
            String symbolFirstWin = messageSymbol.getFirstWin();
            String symbolSecondRemove = messageSymbol.getSecondRemove();
            String symbolSecondWin = messageSymbol.getSecondWin();

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
}
