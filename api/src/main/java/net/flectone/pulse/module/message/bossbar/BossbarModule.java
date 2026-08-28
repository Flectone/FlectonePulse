package net.flectone.pulse.module.message.bossbar;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;

/**
 * Draws the configured boss bars and announces them.
 * @author TheFaser
 */
public interface BossbarModule extends ModuleLocalization {

    @Override
    Message.Bossbar config();

    @Override
    Permission.Message.Bossbar permission();

    @Override
    Localization.Message.Bossbar localization(FPlayer fPlayer);

}
