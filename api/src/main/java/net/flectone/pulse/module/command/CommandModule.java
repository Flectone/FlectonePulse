package net.flectone.pulse.module.command;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.util.constant.ModuleName;
import org.jspecify.annotations.NonNull;

/**
 * Groups every chat command the plugin registers. Disabling it removes them all.
 * @author TheFaser
 */
public interface CommandModule extends ModuleSimple {

    @Override
    ModuleName name();

    @Override
    Command config();

    @Override
    Permission.Command permission();

}
