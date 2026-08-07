package net.flectone.pulse.module.command.tictactoe;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.tictactoe.model.TicTacToe;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;

import java.util.UUID;

/**
 * The /tictactoe command, which plays a round against another player.
 * @author TheFaser
 */
public interface TictactoeModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Tictactoe config();

    @Override
    Permission.Command.Tictactoe permission();

    @Override
    Localization.Command.Tictactoe localization(FPlayer fPlayer);

    /**
     * Invites a player to a game.
     *
     * @param fPlayer who sent the invitation
     * @param fReceiver who was invited
     * @param ticTacToe the game
     * @param metadataUUID the shared message id
     */
    void sendCreateMessage(FPlayer fPlayer, FPlayer fReceiver, TicTacToe ticTacToe, UUID metadataUUID);

    /**
     * Announces a move and redraws the board.
     *
     * @param fPlayer the player who moved
     * @param fReceiver the opponent
     * @param ticTacToe the game
     * @param typeTitle which heading to show
     * @param move the square played
     * @param metadataUUID the shared message id
     */
    void sendMoveMessage(FPlayer fPlayer, FPlayer fReceiver, TicTacToe ticTacToe, int typeTitle, String move, UUID metadataUUID);

    /**
     * Runs the move form of the command.
     *
     * @param fPlayer the player moving
     * @param commandContext the parsed arguments
     */
    void executeMove(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    /**
     * The rendered board plus its heading.
     *
     * @param fPlayer the player who moved
     * @param fResolver the reader
     * @param ticTacToe the game
     * @param typeTile which heading to show
     * @param move the square played
     * @return the rendered board
     */
    String getMoveMessage(FPlayer fPlayer, FPlayer fResolver, TicTacToe ticTacToe, int typeTile, String move);

    /**
     * How far a round has got.
     */
    enum GamePhase {
        CREATE,
        MOVE
    }

}
