package net.flectone.pulse.module.command.tictactoe;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.connector.ProxyConnector;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.tictactoe.manager.TictactoeManager;
import net.flectone.pulse.module.command.tictactoe.model.TicTacToe;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.Optional;
import java.util.function.BiFunction;

@Singleton
public class TictactoeModule extends AbstractModuleCommand<Localization.Command.Tictactoe> {

    private final Command.Tictactoe command;
    private final Permission.Command.Tictactoe permission;

    private final FPlayerService fPlayerService;
    private final TictactoeManager tictactoeManager;
    private final ProxyConnector proxyConnector;
    private final IntegrationModule integrationModule;
    private final CommandRegistry commandRegistry;
    private final Gson gson;

    @Inject
    public TictactoeModule(FileManager fileManager,
                           FPlayerService fPlayerService,
                           TictactoeManager tictactoeManager,
                           ProxyConnector proxyConnector,
                           IntegrationModule integrationModule,
                           CommandRegistry commandRegistry,
                           Gson gson) {
        super(localization -> localization.getCommand().getTictactoe(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.TICTACTOE));

        this.fPlayerService = fPlayerService;
        this.tictactoeManager = tictactoeManager;
        this.proxyConnector = proxyConnector;
        this.integrationModule = integrationModule;
        this.commandRegistry = commandRegistry;
        this.gson = gson;

        command = fileManager.getCommand().getTictactoe();
        permission = fileManager.getPermission().getCommand().getTictactoe();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptPlayer = getPrompt().getPlayer();
        String promptHard = getPrompt().getHard();

        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .required(promptPlayer, commandRegistry.playerParser())
                        .optional(promptHard, commandRegistry.booleanParser())
                        .permission(permission.getName())
                        .handler(this)
        );

        String promptId = getPrompt().getId();
        String promptMove = getPrompt().getMove();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName + "move")
                        .required(promptId, commandRegistry.integerParser())
                        .required(promptMove, commandRegistry.singleMessageParser())
                        .permission(permission.getName())
                        .handler(commandContext -> executeMove(commandContext.sender(), commandContext))
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkMute(fPlayer)) return;

        String promptPlayer = getPrompt().getPlayer();
        String receiverName = commandContext.get(promptPlayer);

        String promptHard = getPrompt().getHard();
        Optional<Boolean> optionalBoolean = commandContext.optional(promptHard);
        boolean isHard = optionalBoolean.orElse(true);

        FPlayer fReceiver = fPlayerService.getFPlayer(receiverName);
        if (!fReceiver.isOnline() || integrationModule.isVanished(fReceiver)) {
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
        if (checkDisable(fPlayer, fReceiver, DisableAction.HE)) return;

        TicTacToe ticTacToe = tictactoeManager.create(fPlayer, fReceiver, isHard);

        builder(fReceiver)
                .receiver(fPlayer)
                .format((fResolver, s) -> s.getSender())
                .sound(getSound())
                .sendBuilt();

        boolean isSent = proxyConnector.sendMessage(fPlayer, MessageTag.COMMAND_TICTACTOE_CREATE, byteArrayDataOutput -> {
            byteArrayDataOutput.writeUTF(gson.toJson(fReceiver));
            byteArrayDataOutput.writeInt(ticTacToe.getId());
            byteArrayDataOutput.writeBoolean(isHard);
        });

        if (isSent) return;

        sendCreateMessage(fPlayer, fReceiver, ticTacToe);
    }

    // /tictactoe %d create
    public void sendCreateMessage(FPlayer fPlayer, FPlayer fReceiver, TicTacToe ticTacToe) {
        if (checkModulePredicates(fPlayer)) return;
        if (integrationModule.isVanished(fReceiver)) return;

        builder(fPlayer)
                .receiver(fReceiver)
                .format(message -> String.format(message.getReceiver(), ticTacToe.getId()))
                .sound(getSound())
                .sendBuilt();
    }

    // /tictactoe %d <move>
    public void sendMoveMessage(FPlayer fPlayer, FPlayer fReceiver, TicTacToe ticTacToe, int typeTitle, String move) {
        if (checkModulePredicates(fPlayer)) return;
        if (integrationModule.isVanished(fReceiver)) return;
        if (ticTacToe == null) return;

        builder(fPlayer)
                .receiver(fReceiver)
                .format(getMoveMessage(ticTacToe, fReceiver, fPlayer, typeTitle, move))
                .sendBuilt();
    }

    public void executeMove(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        String promptId = getPrompt().getId();
        int tictactoeID = commandContext.get(promptId);

        String promptMove = getPrompt().getMove();
        String move = commandContext.get(promptMove);

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
        if (!fReceiver.isOnline() || integrationModule.isVanished(fReceiver)) {
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

        boolean isSent = proxyConnector.sendMessage(fPlayer, MessageTag.COMMAND_TICTACTOE_MOVE, byteArrayDataOutput -> {
            byteArrayDataOutput.writeUTF(gson.toJson(fReceiver));
            byteArrayDataOutput.writeUTF(ticTacToe.toString());
            byteArrayDataOutput.writeInt(finalTypeTitle);
            byteArrayDataOutput.writeUTF(move);
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

            String symbolEmpty = message.getSymbol().getBlank();
            String symbolFirst = message.getSymbol().getFirst();
            String symbolFirstRemove = message.getSymbol().getFirstRemove();
            String symbolFirstWin = message.getSymbol().getFirstWin();
            String symbolSecond = message.getSymbol().getSecond();
            String symbolSecondRemove = message.getSymbol().getSecondRemove();
            String symbolSecondWin = message.getSymbol().getSecondWin();

            String title = (switch (typeTile) {
                case 1 -> message.getFormatWin().replace("<player>", fReceiver.getName());
                case -1 -> message.getFormatDraw();
                default -> message.getFormatMove().replace("<player>", fPlayer.getName());
            });

            String formatField = String.join("<br>", message.getField())
                    .replace("<title>", title)
                    .replace("<current_move>", ticTacToe.isEnded() ? "" : message.getCurrentMove())
                    .replace("<last_move>", message.getLastMove())
                    .replace("<symbol>", ticTacToe.getFirstPlayer() == fPlayer.getId() ? symbolFirst : symbolSecond)
                    .replace("<move>", move);

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
