package net.flectone.pulse.module.message.format.fixation;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleSimple;

/**
 * Cleans up messages before they are shown, trimming repeats and stray characters.
 * @author TheFaser
 */
public interface FixationModule extends ModuleSimple {

    @Override
    Message.Format.Fixation config();

    @Override
    Permission.Message.Format.Fixation permission();

    /**
     * Cleans up the message text.
     *
     * @param messageContext the message being formatted
     * @return the formatted context
     */
    MessageContext format(MessageContext messageContext);

}
