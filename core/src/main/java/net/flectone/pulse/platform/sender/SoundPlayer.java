package net.flectone.pulse.platform.sender;

import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntitySoundEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.service.FPlayerService;

import java.util.Arrays;
import java.util.Optional;

@Singleton
public class SoundPlayer {

    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PacketSender packetSender;

    @Inject
    public SoundPlayer(FPlayerService fPlayerService,
                       PlatformPlayerAdapter platformPlayerAdapter,
                       PacketSender packetSender) {
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.packetSender = packetSender;
    }

    public void play(Sound sound, FPlayer fPlayer) {
        if (!sound.isEnable()) return;

        Optional<SoundCategory> category = getSoundCategory(sound);
        if (category.isEmpty()) return;

        com.github.retrooper.packetevents.protocol.sound.Sound packetSound = Sounds.getByName(sound.getName());
        if (packetSound == null) return;

        packetSender.send(fPlayer, new WrapperPlayServerEntitySoundEffect(packetSound, category.get(),
                platformPlayerAdapter.getEntityId(fPlayer.getUuid()),
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
                .forEach(fReceiver -> packetSender.send(fReceiver, new WrapperPlayServerSoundEffect(packetSound, category.get(),
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
