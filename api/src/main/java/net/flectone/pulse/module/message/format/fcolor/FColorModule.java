package net.flectone.pulse.module.message.format.fcolor;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleSimple;

/**
 * Applies the personal colors a player chose with /chatcolor.
 * @author TheFaser
 */
public interface FColorModule extends ModuleSimple {

    @Override
    Message.Format.FColor config();

    @Override
    Permission.Message.Format.FColor permission();

    /**
     * The formatting permissions this module checks before applying a color.
     *
     * @return the permissions
     */
    Permission.Message.Format formatPermission();

    /**
     * Applies the sender's personal colors to the message.
     *
     * @param messageContext the message being formatted
     * @return the formatted context
     */
    MessageContext format(MessageContext messageContext);

}
