package net.flectone.pulse.module.message.status.motd;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleListLocalization;

/**
 * Replaces the MOTD shown in the server list.
 * @author TheFaser
 */
public interface MotdModule extends ModuleListLocalization {

    @Override
    Message.Status.MOTD config();

    @Override
    Permission.Message.Status.MOTD permission();

    @Override
    Localization.Message.Status.MOTD localization(FPlayer fPlayer);

}
