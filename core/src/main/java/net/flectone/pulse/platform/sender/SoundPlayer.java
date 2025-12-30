package net.flectone.pulse.platform.sender;

import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntitySoundEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
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
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SoundPlayer {

    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PacketSender packetSender;
    private final PermissionChecker permissionChecker;

    /**
     * Plays a sound to a player at their own location.
     *
     * @param soundPermission pair containing sound settings and required permission
     * @param sender the entity triggering the sound (checks permission)
     */
    public void play(Pair<Sound, PermissionSetting> soundPermission, FEntity sender) {
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
    public void play(Pair<Sound, PermissionSetting> soundPermission, FEntity sender, FPlayer receiver) {
        if (soundPermission == null) return;

        Sound sound = soundPermission.first();
        if (!sound.enable()) return;
        if (!permissionChecker.check(sender, soundPermission.second())) return;

        packetSender.send(receiver, new WrapperPlayServerEntitySoundEffect(
                sound.packet(),
                sound.category(),
                platformPlayerAdapter.getEntityId(receiver.getUuid()),
                sound.volume(),
                sound.pitch()
        ));
    }

    /**
     * Plays a sound at a specific location to nearby players (within 16 blocks).
     *
     * @param soundPermission pair containing sound settings and required permission
     * @param sender the player triggering the sound (checks permission)
     * @param vector3i the location to play the sound at (in block coordinates)
     */
    public void play(Pair<Sound, PermissionSetting> soundPermission, FPlayer sender, Vector3i vector3i) {
        if (soundPermission == null) return;

        Sound sound = soundPermission.first();
        if (!sound.enable()) return;
        if (!permissionChecker.check(sender, soundPermission.second())) return;

        fPlayerService.getOnlineFPlayers().stream()
                .filter(fReceiver -> {
                    double distance = platformPlayerAdapter.distance(sender, fReceiver);
                    return distance >= 0 && distance <= 16;
                })
                .forEach(fReceiver -> packetSender.send(fReceiver, new WrapperPlayServerSoundEffect(
                        sound.packet(),
                        sound.category(),
                        vector3i.multiply(8),
                        sound.volume(),
                        sound.pitch()
                )));
    }

}
