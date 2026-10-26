package net.flectone.pulse.module.message.format.moderation.spam;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;

import java.util.UUID;

/**
 * Blocks messages that are sent too often or repeat the previous one.
 * @author TheFaser
 */
public interface SpamModule extends ModuleLocalization {

    @Override
    Localization.Message.Format.Moderation.Spam localization(FPlayer fPlayer);

    @Override
    Message.Format.Moderation.Spam config();

    @Override
    Permission.Message.Format.Moderation.Spam permission();

    /**
     * Compares the message with the previous one and records a violation if it breaks the rule.
     *
     * @param messageContext the message being formatted
     * @return the formatted context
     */
    MessageContext format(MessageContext messageContext);

    /**
     * Whether this player has broken the rule often enough to be punished for it.
     *
     * @param uuid the player
     * @return true if the violation limit has been reached
     */
    boolean isRestricted(UUID uuid);

    /**
     * Forgets the previous message of a player.
     *
     * @param fPlayer the player
     */
    void removeMessage(FPlayer fPlayer);

}
