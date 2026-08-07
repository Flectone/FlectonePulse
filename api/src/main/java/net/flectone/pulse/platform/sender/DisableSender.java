package net.flectone.pulse.platform.sender;

import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.util.constant.ModuleName;

/**
 * Sends disable messages when chat features are disabled for players.
 *
 * @author TheFaser
 * @since 1.6.0
 */
public interface DisableSender {

    /**
     * Checks if a message type is disabled for a receiver and sends appropriate message.
     *
     * @param entity the entity sending the message
     * @param receiver the entity receiving the message
     * @param moduleName the type of message being sent
     * @return true if message type is disabled for receiver, false otherwise
     */
    boolean sendIfDisabled(FEntity entity, FEntity receiver, ModuleName moduleName);

}
