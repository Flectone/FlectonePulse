package net.flectone.pulse.platform.sender;

import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
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

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftSoundPlayer implements SoundPlayer {

    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PacketSender packetSender;
    private final PermissionChecker permissionChecker;

    public void play(Pair<Sound, PermissionSetting> soundPermission, FEntity sender, FPlayer receiver) {
        if (soundPermission == null) return;

        Sound sound = soundPermission.first();
        if (!sound.enable()) return;
        if (!permissionChecker.check(sender, soundPermission.second())) return;

        packetSender.send(receiver, new WrapperPlayServerEntitySoundEffect(
                Sounds.getByNameOrCreate(sound.name()),
                SoundCategory.valueOf(sound.category()),
                platformPlayerAdapter.getEntityId(receiver.getUuid()),
                sound.volume(),
                sound.pitch()
        ));
    }

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
                        Sounds.getByNameOrCreate(sound.name()),
                        SoundCategory.valueOf(sound.category()),
                        vector3i.multiply(8),
                        sound.volume(),
                        sound.pitch()
                )));
    }

}
