package net.flectone.pulse.module.command.unban;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;

/**
 * The /unban command.
 * @author TheFaser
 */
public interface UnbanModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Unban config();

    @Override
    Permission.Command.Unban permission();

    @Override
    Localization.Command.Unban localization(FPlayer fPlayer);

    /**
     * Lifts a ban and announces it.
     *
     * @param fPlayer the moderator
     * @param target the banned player
     * @param id the ban id, or -1 for the most recent
     * @param reason the stated reason
     */
    void unban(FPlayer fPlayer, String target, int id, String reason);

}
