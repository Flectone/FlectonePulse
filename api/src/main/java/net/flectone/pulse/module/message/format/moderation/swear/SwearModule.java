package net.flectone.pulse.module.message.format.moderation.swear;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.constant.ModuleName;

import java.util.UUID;

/**
 * Censors messages that contain forbidden words.
 * @author TheFaser
 */
public interface SwearModule extends ModuleLocalization {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    ModuleName name();

    @Override
    Message.Format.Moderation.Swear config();

    @Override
    Permission.Message.Format.Moderation.Swear permission();

    @Override
    Localization.Message.Format.Moderation.Swear localization(FPlayer fPlayer);

    /**
     * Censors the forbidden words and records a violation if the rule was broken.
     *
     * @param messageContext the message being formatted
     * @return the formatted context
     */
    MessageContext format(MessageContext messageContext);

    /**
     * Registers the tag that renders a censored word.
     *
     * @param messageContext the message being formatted
     * @return the context with the tag added
     */
    MessageContext addTag(MessageContext messageContext);

    /**
     * Whether this player has broken the rule often enough to be punished for it.
     *
     * @param uuid the player
     * @return true if the violation limit has been reached
     */
    boolean isRestricted(UUID uuid);

}
