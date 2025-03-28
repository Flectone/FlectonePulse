package net.flectone.pulse.platform;

import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntitySoundEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Sound;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.PacketEventsUtil;

import java.util.Arrays;
import java.util.Optional;

@Singleton
public class SoundPlayer {

    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PacketEventsUtil packetEventsUtil;

    @Inject
    public SoundPlayer(FPlayerService fPlayerService,
                       PlatformPlayerAdapter platformPlayerAdapter,
                       PacketEventsUtil packetEventsUtil) {
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.packetEventsUtil = packetEventsUtil;
    }

    public void play(Sound sound, FPlayer fPlayer) {
        if (!sound.isEnable()) return;

        Optional<SoundCategory> category = getSoundCategory(sound);
        if (category.isEmpty()) return;

        com.github.retrooper.packetevents.protocol.sound.Sound packetSound = Sounds.getByName(sound.getName());
        if (packetSound == null) return;

        packetEventsUtil.sendPacket(fPlayer, new WrapperPlayServerEntitySoundEffect(packetSound, category.get(),
                fPlayer.getEntityId(),
                sound.getVolume(),
                sound.getPitch()
        ));
    }

    public void play(Sound sound, FPlayer fPlayer, Vector3i vector3i) {
        if (!sound.isEnable()) return;

        Optional<SoundCategory> category = getSoundCategory(sound);
        if (category.isEmpty()) return;

        com.github.retrooper.packetevents.protocol.sound.Sound packetSound = Sounds.getByName(sound.getName());
        if (packetSound == null) return;

        fPlayerService.getFPlayers().stream()
                .filter(fReceiver -> {
                    double distance = platformPlayerAdapter.distance(fPlayer, fReceiver);
                    return distance >= 0 && distance <= 16;
                })
                .forEach(fReceiver -> packetEventsUtil.sendPacket(fReceiver, new WrapperPlayServerSoundEffect(packetSound, category.get(),
                        vector3i.multiply(8),
                        sound.getVolume(),
                        sound.getPitch()
                )));
    }

    public Optional<SoundCategory> getSoundCategory(Sound sound) {
        return Arrays.stream(SoundCategory.values())
                .filter(soundCategory -> soundCategory.name().equalsIgnoreCase(sound.getCategory()))
                .findAny();
    }

}
