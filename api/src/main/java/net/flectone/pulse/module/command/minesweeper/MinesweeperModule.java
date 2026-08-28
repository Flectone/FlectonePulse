package net.flectone.pulse.module.command.minesweeper;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;

import java.util.UUID;

/**
 * The /minesweeper command, which plays a game of minesweeper in chat.
 * @author TheFaser
 */
public interface MinesweeperModule extends ModuleCommand {

    /**
     * Forgets a player's board.
     *
     * @param uuid the player
     */
    void removeGame(UUID uuid);

    @Override
    Command.Minesweeper config();

    @Override
    Permission.Command.Minesweeper permission();

    @Override
    Localization.Command.Minesweeper localization(FPlayer fPlayer);

}
