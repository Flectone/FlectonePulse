package net.flectone.pulse.platform.render;

import net.flectone.pulse.model.entity.FPlayer;
import net.kyori.adventure.text.Component;

/**
 * Renders action bar messages to players with version compatibility.
 * Supports all Minecraft versions from 1.8 to latest.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * ActionBarRender actionBar = flectonePulse.get(ActionBarRender.class);
 *
 * // Send a temporary action bar
 * actionBar.render(player, Component.text("Hello!"), 60); // 3 seconds
 * }</pre>
 *
 * @since 1.7.0
 * @author TheFaser
 */
public interface ActionBarRender {

    /**
     * Renders an action bar message with default duration (30 ticks).
     *
     * @param fPlayer the player to receive the action bar
     * @param component the message component to display
     */
    default void render(FPlayer fPlayer, Component component) {
        render(fPlayer, component, 0);
    }

    /**
     * Renders an action bar message with custom duration.
     *
     * @param fPlayer the player to receive the action bar
     * @param component the message component to display
     * @param stayTicks duration in ticks (20 ticks = 1 second, 0 = default)
     */
    void render(FPlayer fPlayer, Component component, int stayTicks);

}
