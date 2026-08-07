package net.flectone.pulse.module.command.unmute;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import org.incendo.cloud.context.CommandContext;

/**
 * The /unmute command.
 * @author TheFaser
 */
public interface UnmuteModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Unmute config();

    @Override
    Permission.Command.Unmute permission();

    @Override
    Localization.Command.Unmute localization(FPlayer fPlayer);

    /**
     * Lifts a mute and announces it.
     *
     * @param fPlayer the moderator
     * @param target the muted player
     * @param id the mute id, or -1 for the most recent
     * @param reason the stated reason
     */
    void unmute(FPlayer fPlayer, String target, int id, String reason);

}
