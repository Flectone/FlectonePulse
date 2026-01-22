package net.flectone.pulse.platform.render;

import net.flectone.pulse.model.entity.FPlayer;
import net.kyori.adventure.text.Component;

/**
 * Renders header and footer components in player's tab list.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * ListFooterRender render = flectonePulse.get(ListFooterRender.class);
 *
 * // Set tab list header and footer
 * render.render(player,
 *     Component.text("Server Header"),
 *     Component.text("Online: 50")
 * );
 * }</pre>
 *
 * @since 1.7.0
 * @author TheFaser
 */
public interface ListFooterRender {

    /**
     * Renders header and footer components to a player's tab list.
     *
     * @param fPlayer the player to receive the tab list update
     * @param header the header component to display
     * @param footer the footer component to display
     */
    void render(FPlayer fPlayer, Component header, Component footer);

}
