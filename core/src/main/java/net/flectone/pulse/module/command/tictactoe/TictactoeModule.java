package net.flectone.pulse.module.command.tictactoe;

import com.google.gson.Gson;
import com.google.inject.Inject;
import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.connector.ProxyConnector;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.database.dao.IgnoreDAO;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.tictactoe.manager.TictactoeManager;
import net.flectone.pulse.module.command.tictactoe.model.TicTacToe;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;

import java.util.function.BiFunction;

public abstract class TictactoeModule extends AbstractModuleCommand<Localization.Command.Tictactoe> {

    @Getter private final Command.Tictactoe command;
    @Getter private final Permission.Command.Tictactoe permission;

    private final FPlayerDAO fPlayerDAO;
    private final IgnoreDAO ignoreDAO;
    private final TictactoeManager tictactoeManager;
    private final ProxyConnector proxyConnector;
    private final IntegrationModule integrationModule;
    private final CommandUtil commandUtil;
    private final Gson gson;

    @Inject
    public TictactoeModule(FileManager fileManager,
                           FPlayerDAO fPlayerDAO,
                           IgnoreDAO ignoreDAO,
                           TictactoeManager tictactoeManager,
                           ProxyConnector proxyConnector,
                           IntegrationModule integrationModule,
                           CommandUtil commandUtil,
                           Gson gson) {
        super(localization -> localization.getCommand().getTictactoe(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.TICTACTOE));

        this.fPlayerDAO = fPlayerDAO;
        this.ignoreDAO = ignoreDAO;
        this.tictactoeManager = tictactoeManager;
        this.proxyConnector = proxyConnector;
        this.integrationModule = integrationModule;
        this.commandUtil = commandUtil;
        this.gson = gson;

        command = fileManager.getCommand().getTictactoe();
        permission = fileManager.getPermission().getCommand().getTictactoe();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkMute(fPlayer)) return;

        String receiverName = commandUtil.getString(0, arguments);
        boolean isHard = commandUtil.getByClassOrDefault(1, Boolean.class, true, arguments);

        FPlayer fReceiver = fPlayerDAO.getFPlayer(receiverName);
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

        ignoreDAO.load(fReceiver);

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
                .format(message -> String.format(message.getFormatCreate(), ticTacToe.getId()))
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

    @Async
    public void move(FPlayer fPlayer, Object arguments) {
        int tictactoeID = commandUtil.getInteger(0, arguments);
        String move = commandUtil.getByClassOrDefault(1, String.class, "create", arguments);

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

        FPlayer fReceiver = fPlayerDAO.getFPlayer(ticTacToe.getNextPlayer());
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

            String symbolEmpty = message.getSymbol().getEmpty();
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

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}
