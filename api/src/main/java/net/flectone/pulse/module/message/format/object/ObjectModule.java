package net.flectone.pulse.module.message.format.object;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;

/**
 * Inserts item, entity and player-head objects into chat.
 * @author TheFaser
 */
public interface ObjectModule extends ModuleLocalization {

    @Override
    Localization.Message.Format.Object localization(FPlayer fPlayer);

    @Override
    Message.Format.Object config();

    @Override
    Permission.Message.Format.Object permission();

}
