package net.flectone.pulse.module.command.rockpaperscissors;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import org.incendo.cloud.context.CommandContext;

import java.util.UUID;

/**
 * The /rockpaperscissors command, which plays a round against another player.
 * @author TheFaser
 */
public interface RockpaperscissorsModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Rockpaperscissors config();

    @Override
    Permission.Command.Rockpaperscissors permission();

    @Override
    Localization.Command.Rockpaperscissors localization(FPlayer fPlayer);

    /**
     * Records the second player's move and announces the winner.
     *
     * @param fPlayer the player moving
     * @param fReceiver the opponent
     * @param move the chosen shape
     * @param uuid the game id
     */
    void finalMove(FPlayer fPlayer, FPlayer fReceiver, String move, UUID uuid);

    /**
     * Closes a game and announces the result.
     *
     * @param id the game id
     * @param fPlayer the player who moved last
     * @param move their shape
     * @param metadataUUID the shared message id
     */
    void end(UUID id, FPlayer fPlayer, String move, UUID metadataUUID);

    /**
     * Records a move.
     *
     * @param id the game id
     * @param fPlayer the player moving
     * @param move the chosen shape
     * @param metadataUUID the shared message id
     */
    void move(UUID id, FEntity fPlayer, String move, UUID metadataUUID);

    /**
     * Invites a player to a game.
     *
     * @param id the game id
     * @param fPlayer who sent the invitation
     * @param receiver who was invited
     */
    void create(UUID id, FEntity fPlayer, UUID receiver);

    /**
     * How far a round has got.
     */
    enum GamePhase {
        CREATE,
        MOVE,
        END
    }

}
