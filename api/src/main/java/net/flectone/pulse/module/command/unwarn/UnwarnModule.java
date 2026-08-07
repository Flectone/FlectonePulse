package net.flectone.pulse.module.command.unwarn;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;

/**
 * The /unwarn command.
 * @author TheFaser
 */
public interface UnwarnModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Unwarn config();

    @Override
    Permission.Command.Unwarn permission();

    @Override
    Localization.Command.Unwarn localization(FPlayer fPlayer);

    /**
     * Lifts a warning and announces it.
     *
     * @param fPlayer the moderator
     * @param target the warned player
     * @param id the warning id, or -1 for the most recent
     * @param reason the stated reason
     */
    void unwarn(FPlayer fPlayer, String target, int id, String reason);

}
