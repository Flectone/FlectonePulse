package net.flectone.pulse.module.message.auto;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Pair;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.module.ModuleListLocalization;
import net.flectone.pulse.util.constant.ModuleName;

/**
 * Broadcasts the configured messages on a timer.
 * @author TheFaser
 */
public interface AutoModule extends ModuleListLocalization {

    @Override
    ModuleName name();

    @Override
    Message.Auto config();

    @Override
    Permission.Message.Auto permission();

    @Override
    Localization.Message.Auto localization(FPlayer fPlayer);

    /**
     * Sends one scheduled broadcast.
     *
     * @param fPlayer the reader
     * @param name the config entry the message comes from
     * @param type the broadcast settings
     * @param sound the sound and the permission to hear it
     */
    void send(FPlayer fPlayer, String name, Message.Auto.Type type, Pair<Sound, PermissionSetting> sound);

}
