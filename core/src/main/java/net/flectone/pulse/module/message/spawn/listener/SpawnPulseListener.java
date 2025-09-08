package net.flectone.pulse.module.message.spawn.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.spawn.SpawnModule;
import net.flectone.pulse.module.message.spawn.extractor.SpawnExtractor;
import net.flectone.pulse.module.message.spawn.model.Spawn;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class SpawnPulseListener implements PulseListener {

    private final SpawnModule spawnModule;
    private final SpawnExtractor spawnExtractor;

    @Inject
    public SpawnPulseListener(SpawnModule spawnModule,
                              SpawnExtractor spawnExtractor) {
        this.spawnModule = spawnModule;
        this.spawnExtractor = spawnExtractor;
    }


    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        FPlayer fPlayer = event.getFPlayer();
        MinecraftTranslationKey key = event.getTranslationKey();
        if (key == MinecraftTranslationKey.BLOCK_MINECRAFT_SET_SPAWN) {
            event.setCancelled(true);
            spawnModule.send(fPlayer, MinecraftTranslationKey.BLOCK_MINECRAFT_SET_SPAWN);
            return;
        }

        if (!key.startsWith("commands.spawnpoint.success")
                && key != MinecraftTranslationKey.COMMANDS_SETWORLDSPAWN_SUCCESS) return;

        Optional<Spawn> spawn = spawnExtractor.extract(event);
        if (spawn.isEmpty()) return;

        event.setCancelled(true);
        spawnModule.send(fPlayer, key, spawn.get());
    }

}
