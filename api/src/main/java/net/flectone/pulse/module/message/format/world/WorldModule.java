package net.flectone.pulse.module.message.format.world;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleSimple;

/**
 * Inserts the sender's world into a message, by name or by type.
 * @author TheFaser
 */
public interface WorldModule extends ModuleSimple {

    @Override
    Message.Format.World config();

    @Override
    Permission.Message.Format.World permission();

    /**
     * Registers the tag that renders the sender's world.
     *
     * @param messageContext the message being formatted
     * @return the context with the tag added
     */
    MessageContext addTag(MessageContext messageContext);

    /**
     * Refreshes the world recorded for a player after they change dimension.
     *
     * @param fPlayer the player
     */
    void update(FPlayer fPlayer);

    /**
     * Whether the world is shown by name or by type.
     */
    enum Mode {
        TYPE,
        NAME
    }

}
