package net.flectone.pulse.platform.render;

import com.github.retrooper.packetevents.protocol.player.User;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Times;
import net.flectone.pulse.platform.provider.PacketProvider;
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
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TitleRender {

    private final PacketProvider packetProvider;

    /**
     * Renders a title and subtitle to a player with timing control.
     *
     * @param fPlayer the player to receive the title
     * @param title the main title component
     * @param subTitle the subtitle component (empty for no subtitle)
     * @param times timing configuration for fade in, stay, and fade out
     */
    public void render(FPlayer fPlayer, Component title, Component subTitle, Times times) {
        User user = packetProvider.getUser(fPlayer);
        if (user == null) return;

        user.sendTitle(title, subTitle, times.fadeInTicks(), times.stayTicks(), times.fadeOutTicks());
    }

}
