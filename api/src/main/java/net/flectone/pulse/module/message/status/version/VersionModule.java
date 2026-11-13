package net.flectone.pulse.module.message.status.version;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;

/**
 * Replaces the version name shown in the server list.
 * @author TheFaser
 */
public interface VersionModule extends ModuleLocalization {

    @Override
    Message.Status.Version config();

    @Override
    Permission.Message.Status.Version permission();

    @Override
    Localization.Message.Status.Version localization(FPlayer fPlayer);

    /**
     * The version name shown to a player.
     *
     * @param fPlayer the player
     * @return the version name, or null if the module is disabled
     */
    String get(FPlayer fPlayer);

}
