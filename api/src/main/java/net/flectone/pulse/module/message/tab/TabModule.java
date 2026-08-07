package net.flectone.pulse.module.message.tab;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.util.constant.ModuleName;

/**
 * Rewrites the tab list header, footer and player names.
 * @author TheFaser
 */
public interface TabModule extends ModuleSimple {

    @Override
    ModuleName name();

    @Override
    Message.Tab config();

    @Override
    Permission.Message.Tab permission();

}
