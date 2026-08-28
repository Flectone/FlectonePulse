package net.flectone.pulse.module.message.format.mention;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;

/**
 * Highlights a player's name in chat and notifies them.
 * @author TheFaser
 */
public interface MentionModule extends ModuleLocalization {

    @Override
    Message.Format.Mention config();

    @Override
    Permission.Message.Format.Mention permission();

    @Override
    Localization.Message.Format.Mention localization(FPlayer fPlayer);

    /**
     * Finds the mentioned players and marks them in the message.
     *
     * @param messageContext the message being formatted
     * @return the formatted context
     */
    MessageContext format(MessageContext messageContext);

    /**
     * Registers the tags that highlight mentioned names.
     *
     * @param messageContext the message being formatted
     * @return the context with the tags added
     */
    MessageContext addTags(MessageContext messageContext);

    /**
     * Notifies a player that they were mentioned.
     *
     * @param fPlayer the mentioned player
     */
    void sendMention(FPlayer fPlayer);

}
