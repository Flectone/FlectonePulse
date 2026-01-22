package net.flectone.pulse.platform.sender;

import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Sound;
import org.incendo.cloud.type.tuple.Pair;

/**
 * Plays sounds to players with permission checking.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * SoundPlayer soundPlayer = flectonePulse.get(SoundPlayer.class);
 *
 * Sound sound = ...;
 * PermissionSetting permission = new PermissionSetting("myplugin.sound", false);
 *
 * // Play sound to specific player
 * soundPlayer.play(Pair.of(sound, permission), sender, receiver);
 *
 * // Play sound at location to nearby players
 * soundPlayer.play(Pair.of(sound, permission), sender, new Vector3i(100, 64, 200));
 * }</pre>
 *
 * @since 1.6.0
 * @author TheFaser
 */
public interface SoundPlayer {

    /**
     * Plays a sound to a player at their own location.
     *
     * @param soundPermission pair containing sound settings and required permission
     * @param sender the entity triggering the sound (checks permission)
     */
    default void play(Pair<Sound, PermissionSetting> soundPermission, FEntity sender) {
        if (sender instanceof FPlayer fPlayer) {
            play(soundPermission, fPlayer, fPlayer);
        }
    }

    /**
     * Plays a sound from one player to another player.
     *
     * @param soundPermission pair containing sound settings and required permission
     * @param sender the player triggering the sound (checks permission)
     * @param receiver the player receiving the sound
     */
    void play(Pair<Sound, PermissionSetting> soundPermission, FEntity sender, FPlayer receiver);

}
