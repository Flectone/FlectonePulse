package net.flectone.pulse.module.message.rightclick;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;

import java.util.UUID;

/**
 * Announces that one player right-clicked another.
 * @author TheFaser
 */
public interface RightclickModule extends ModuleLocalization {

    @Override
    Message.Rightclick config();

    @Override
    Permission.Message.Rightclick permission();

    @Override
    Localization.Message.Rightclick localization(FPlayer fPlayer);

    /**
     * Announces that one player right-clicked another.
     *
     * @param uuid the clicking player
     * @param targetId the clicked player id
     */
    void send(UUID uuid, int targetId);

}
