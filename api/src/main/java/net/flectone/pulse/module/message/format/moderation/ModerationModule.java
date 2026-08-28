package net.flectone.pulse.module.message.format.moderation;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.ModuleSimple;

/**
 * Groups the chat rules that can silence or punish a player.
 * @author TheFaser
 */
public interface ModerationModule extends ModuleSimple {

    @Override
    Message.Format.Moderation config();

    @Override
    Permission.Message.Format.Moderation permission();

}
