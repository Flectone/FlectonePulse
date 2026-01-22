package net.flectone.pulse.platform.render;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Times;
import net.kyori.adventure.text.Component;

/**
 * Renders title and subtitle displays to players.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * TitleRender titleRender = flectonePulse.get(TitleRender.class);
 *
 * Times times = new Times(10, 40, 10); // fade in, stay, fade out in ticks
 * titleRender.render(player,
 *     Component.text("Main Title"),
 *     Component.text("Subtitle"),
 *     times
 * );
 * }</pre>
 *
 * @since 1.7.0
 * @author TheFaser
 */
public interface TitleRender {

    /**
     * Renders a title and subtitle to a player with timing control.
     *
     * @param fPlayer the player to receive the title
     * @param title the main title component
     * @param subTitle the subtitle component (empty for no subtitle)
     * @param times timing configuration for fade in, stay, and fade out
     */
    void render(FPlayer fPlayer, Component title, Component subTitle, Times times);

}
