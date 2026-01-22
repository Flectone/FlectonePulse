package net.flectone.pulse.platform.render;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.TextScreen;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.UUID;

/**
 * Renders text display entities attached to players with animations and effects.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * TextScreenRender render = flectonePulse.get(TextScreenRender.class);
 *
 * TextScreen textScreen = new TextScreen(
 *     5, // live time in seconds
 *     0.5f, // scale
 *     100, // width
 *     true, // shadow
 *     "#FF0000", // background color
 *     true, // see through
 *     0, 1, 0, // offset
 *     true, 10 // animation enabled, 10 ticks
 * );
 *
 * render.render(player, Component.text("Hello!"), textScreen);
 * }</pre>
 *
 * @since 1.7.0
 * @author TheFaser
 */
public interface TextScreenRender {

    /**
     * Clears all active text screen entities for all players.
     */
    void clear();

    /**
     * Renders a text screen entity attached to a player.
     *
     * @param fPlayer the player to attach the text screen to
     * @param message the text component to display
     * @param textScreen the text screen configuration
     */
    void render(FPlayer fPlayer, Component message, TextScreen textScreen);

    /**
     * Gets all text screen entity IDs attached to a player.
     *
     * @param uuid the player's UUID
     * @return list of entity IDs, empty if none
     */
    List<Integer> getPassengers(UUID uuid);

    /**
     * Attaches text screen entities to a player as passengers.
     *
     * @param uuid the player's UUID
     * @param playerId the player's entity ID
     * @param textScreenPassengers list of text screen entity IDs
     * @param silent whether to send the packet silently
     */
    void ride(UUID uuid, int playerId, List<Integer> textScreenPassengers, boolean silent);


    /**
     * Updates passenger list for a player asynchronously.
     *
     * @param playerId the player's entity ID
     */
    void updateAndRide(int playerId);

}
