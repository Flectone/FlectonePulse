package net.flectone.pulse.module.message.format.moderation.caps;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;

import java.util.UUID;

/**
 * Blocks messages written mostly in capitals.
 * @author TheFaser
 */
public interface CapsModule extends ModuleLocalization {

    @Override
    void onEnable();

    @Override
    Localization.Message.Format.Moderation.Caps localization(FPlayer fPlayer);

    @Override
    ModuleName name();

    @Override
    Message.Format.Moderation.Caps config();

    @Override
    Permission.Message.Format.Moderation.Caps permission();

    /**
     * Checks the message for excessive capitals and records a violation if it breaks the rule.
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
