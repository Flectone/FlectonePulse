package net.flectone.pulse.module.message.status;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.util.constant.ModuleName;

/**
 * Rewrites the server list entry, meaning the MOTD, icon, player count and version text.
 * @author TheFaser
 */
public interface StatusModule extends ModuleSimple {

    @Override
    ModuleName name();

    @Override
    Message.Status config();

    @Override
    Permission.Message.Status permission();

}
