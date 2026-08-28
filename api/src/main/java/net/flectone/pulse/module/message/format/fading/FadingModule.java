package net.flectone.pulse.module.message.format.fading;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.message.context.StringMessageContext;
import net.flectone.pulse.module.ModuleLocalization;

/**
 * Fades a message between two colors.
 * @author TheFaser
 */
public interface FadingModule extends ModuleLocalization {

    @Override
    Localization.Message.Format.Fading localization(FPlayer fPlayer);

    @Override
    Message.Format.Fading config();

    @Override
    Permission.Message.Format.Fading permission();

    /**
     * Registers the tag that fades text between two colors.
     *
     * @param messageContext the message being formatted
     * @return the context with the tag added
     */
    MessageContext addTag(StringMessageContext messageContext);

}
