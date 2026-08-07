package net.flectone.pulse.model.event.message;

import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

/**
 * Fired when the server is about to show a message to a player, including messages FlectonePulse did not send.
 *
 * @param cancelled whether a listener suppressed the message
 * @param player the receiving player
 * @param component the message
 * @param overlay whether it is drawn over the hotbar instead of in chat
 * @author TheFaser
 */
@With
public record MessageReceiveEvent(
        boolean cancelled,
        FPlayer player,
        Component component,
        boolean overlay
) implements Event {

    /**
     * Creates an event that has not been cancelled.
     *
     * @param fPlayer the receiving player
     * @param component the message
     * @param overlay whether it is drawn over the hotbar
     */
    public MessageReceiveEvent(FPlayer fPlayer, Component component, boolean overlay) {
        this(false, fPlayer, component, overlay);
    }

    /**
     * The message as a translatable component, which vanilla messages usually are.
     *
     * @return the translatable component, or null if the message is not translatable
     */
    public TranslatableComponent getTranslatableComponent() {
        return component instanceof TranslatableComponent translatableComponent ? translatableComponent : null;
    }

}