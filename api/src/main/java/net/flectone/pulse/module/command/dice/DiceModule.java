package net.flectone.pulse.module.command.dice;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;

import java.util.List;

/**
 * The /dice command, which rolls one or more dice.
 * @author TheFaser
 */
public interface DiceModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Dice config();

    @Override
    Permission.Command.Dice permission();

    @Override
    Localization.Command.Dice localization(FPlayer fPlayer);

    /**
     * The wording of a roll result in the reader's language.
     *
     * @param fPlayer the reader
     * @param cubes the value rolled on each die
     * @return the result text
     */
    String replaceResult(FPlayer fPlayer, List<Integer> cubes);

}
