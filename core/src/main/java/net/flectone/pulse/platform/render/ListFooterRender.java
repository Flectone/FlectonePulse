package net.flectone.pulse.platform.render;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.sender.PacketSender;
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
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ListFooterRender {

    private final PacketSender packetSender;

    /**
     * Renders header and footer components to a player's tab list.
     *
     * @param fPlayer the player to receive the tab list update
     * @param header the header component to display
     * @param footer the footer component to display
     */
    public void render(FPlayer fPlayer, Component header, Component footer) {
        packetSender.send(fPlayer, new WrapperPlayServerPlayerListHeaderAndFooter(header, footer));
    }

}
