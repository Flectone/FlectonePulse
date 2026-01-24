package net.flectone.pulse.platform.render;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Toast;
import net.kyori.adventure.text.Component;

/**
 * Renders toast notifications (advancement-style popups) to players.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * ToastRender toastRender = flectonePulse.get(ToastRender.class);
 *
 * Toast toast = new Toast(
 *     Toast.Style.TASK,
 *     "minecraft:diamond"
 * );
 *
 * toastRender.render(player, Component.text("Achievement Unlocked!"), toast);
 * }</pre>
 *
 * @since 1.7.0
 * @author TheFaser
 */
public interface ToastRender {

    /**
     * Renders a toast notification to a player.
     *
     * @param fPlayer the player to receive the toast
     * @param title the title component to display
     * @param description the description component to display
     * @param toast the toast configuration including style and icon
     */
    void render(FPlayer fPlayer, Component title, Component description, Toast toast);

}
