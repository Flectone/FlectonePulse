package net.flectone.pulse.module.command.coin;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import org.incendo.cloud.context.CommandContext;

/**
 * The /coin command, which flips a coin.
 * @author TheFaser
 */
public interface CoinModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Coin config();

    @Override
    Permission.Command.Coin permission();

    @Override
    Localization.Command.Coin localization(FPlayer fPlayer);

    /**
     * The wording of a flip result in the reader's language.
     *
     * @param fPlayer the reader
     * @param percent the rolled percentage
     * @return the result text
     */
    String replaceResult(FPlayer fPlayer, int percent);

}
