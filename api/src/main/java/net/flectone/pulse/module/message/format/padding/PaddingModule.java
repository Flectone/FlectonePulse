package net.flectone.pulse.module.message.format.padding;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;

/**
 * Pads a message with a filler so it lines up left, right or centred.
 * @author TheFaser
 */
public interface PaddingModule extends ModuleLocalization {

    @Override
    void onEnable();

    @Override
    ModuleName name();

    @Override
    Message.Format.Padding config();

    @Override
    Permission.Message.Format.Padding permission();

    @Override
    Localization.Message.Format.Padding localization(FPlayer fPlayer);

    /**
     * Registers the tag that pads a message to a fixed width.
     *
     * @param messageContext the message being formatted
     * @return the context with the tag added
     */
    MessageContext addTag(MessageContext messageContext);

}
