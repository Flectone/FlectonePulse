package net.flectone.pulse.module.message.format.names;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.util.constant.ModuleName;

/**
 * Builds the name shown for a sender, including prefixes, suffixes and the invisible-player form.
 * @author TheFaser
 */
public interface NamesModule extends ModuleLocalization {

    @Override
    void onEnable();

    @Override
    ModuleName name();

    @Override
    Message.Format.Names config();

    @Override
    Permission.Message.Format.Names permission();

    @Override
    Localization.Message.Format.Names localization(FPlayer fPlayer);

    /**
     * Registers the tags that render a sender's name, prefix and suffix.
     *
     * @param messageContext the message being formatted
     * @return the context with the tags added
     */
    MessageContext addTags(MessageContext messageContext);

    /**
     * Registers the tag that hides a name when the sender should not be identified.
     *
     * @param messageContext the message being formatted
     * @return the context with the tag added
     */
    MessageContext addInvisibleTag(MessageContext messageContext);

    /**
     * Whether this entity's name should be hidden, because it is vanished or drinking an invisibility potion.
     *
     * @param entity the entity
     * @return true if the name is hidden
     */
    boolean isInvisible(FEntity entity);

}
