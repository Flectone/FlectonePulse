package net.flectone.pulse.platform.render;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.BossBar;
import net.kyori.adventure.text.Component;

/**
 * Renders boss bar displays to players with automatic cleanup.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * BossBarRender bossBarRender = flectonePulse.get(BossBarRender.class);
 *
 * BossBar bossBar = new BossBar(
 *     0.5f, // 50% health
 *     Color.BLUE,
 *     Overlay.PROGRESS,
 *     Set.of(Flag.DARKEN_SKY),
 *     100 // 5 seconds
 * );
 *
 * bossBarRender.render(player, Component.text("Boss Fight!"), bossBar);
 * }</pre>
 *
 * @since 1.7.0
 * @author TheFaser
 */
public interface BossBarRender {

    /**
     * Renders a boss bar to a player with automatic removal.
     *
     * @param fPlayer the player to receive the boss bar
     * @param component the title component to display
     * @param bossBar the boss bar configuration
     */
    void render(FPlayer fPlayer, Component component, BossBar bossBar);

}
