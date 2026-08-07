package net.flectone.pulse.module.message.scoreboard.objective;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.util.constant.ModuleName;

/**
 * Groups the scoreboard objectives shown below a player's name and in the tab list.
 * @author TheFaser
 */
public interface ObjectiveModule extends ModuleSimple {

    @Override
    ModuleName name();

    @Override
    Message.Scoreboard.Objective config();

    @Override
    Permission.Message.Scoreboard.Objective permission();

}
