package net.flectone.pulse.module.message;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.module.ModuleSimple;

/**
 * Groups every message the plugin sends on its own. Disabling it silences them all.
 * @author TheFaser
 */
public interface MessageModule extends ModuleSimple {

    @Override
    ModuleName name();

    @Override
    Message config();

    @Override
    Permission.Message permission();

}
