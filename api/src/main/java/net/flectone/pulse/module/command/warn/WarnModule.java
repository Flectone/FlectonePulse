package net.flectone.pulse.module.command.warn;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.Moderation;
import net.flectone.pulse.module.ModuleCommand;
import org.incendo.cloud.context.CommandContext;

/**
 * The /warn command.
 * @author TheFaser
 */
public interface WarnModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Warn config();

    @Override
    Permission.Command.Warn permission();

    @Override
    Localization.Command.Warn localization(FPlayer fPlayer);

    /**
     * Tells the warned player about the warning.
     *
     * @param warn the warning that was issued
     */
    void sendForTarget(Moderation warn);

}
