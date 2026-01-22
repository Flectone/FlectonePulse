package net.flectone.pulse.platform.sender;

import net.flectone.pulse.model.entity.FPlayer;
import net.kyori.adventure.text.Component;

/**
 * Sends chat messages to players and console with proper version compatibility.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * MessageSender messageSender = flectonePulse.get(MessageSender.class);
 *
 * // Send message to player
 * messageSender.sendMessage(player, Component.text("Hello!"), false);
 *
 * // Send message to console
 * messageSender.sendToConsole(Component.text("Server message"));
 * }</pre>
 *
 * @since 1.2.0
 * @author TheFaser
 */
public interface MessageSender {

    /**
     * Sends a formatted message to the server console.
     *
     * @param component the component to send to console
     */
    void sendToConsole(Component component);

    /**
     * Sends a chat message to a player.
     *
     * @param fPlayer the player or console to receive the message
     * @param component the message component to send
     * @param silent whether to send the packet silently
     */
    void sendMessage(FPlayer fPlayer, Component component, boolean silent);

}
