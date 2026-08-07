package net.flectone.pulse.module.command.minesweeper;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;

import java.util.UUID;

/**
 * The /minesweeper command, which plays a game of minesweeper in chat.
 * @author TheFaser
 */
public interface MinesweeperModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    /**
     * Forgets a player's board.
     *
     * @param uuid the player
     */
    void removeGame(UUID uuid);

    @Override
    ModuleName name();

    @Override
    Command.Minesweeper config();

    @Override
    Permission.Command.Minesweeper permission();

    @Override
    Localization.Command.Minesweeper localization(FPlayer fPlayer);

}
