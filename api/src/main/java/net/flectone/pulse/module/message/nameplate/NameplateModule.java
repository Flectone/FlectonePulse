package net.flectone.pulse.module.message.nameplate;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;

/**
 * Displays nameplates above players' heads for nearby viewers.
 * @author TheFaser
 */
public interface NameplateModule extends ModuleLocalization {

    @Override
    Message.Nameplate config();

    @Override
    Permission.Message.Nameplate permission();

    @Override
    Localization.Message.Nameplate localization(FPlayer fPlayer);

    /**
     * Shows the nameplate for the given player to nearby viewers.
     *
     * @param fPlayer the player whose nameplate is shown
     */
    void show(FPlayer fPlayer);

}