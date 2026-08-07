package net.flectone.pulse.module.message.format.moderation.flood;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;

import java.util.UUID;

/**
 * Blocks messages that repeat characters or words.
 * @author TheFaser
 */
public interface FloodModule extends ModuleLocalization {

    @Override
    void onEnable();

    @Override
    Localization.Message.Format.Moderation.Flood localization(FPlayer fPlayer);

    @Override
    ModuleName name();

    @Override
    Message.Format.Moderation.Flood config();

    @Override
    Permission.Message.Format.Moderation.Flood permission();

    /**
     * Checks the message for repetition and records a violation if it breaks the rule.
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

}
