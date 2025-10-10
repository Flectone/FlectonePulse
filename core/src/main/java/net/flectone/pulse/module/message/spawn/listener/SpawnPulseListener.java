package net.flectone.pulse.module.message.spawn.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpawnPulseListener implements PulseListener {

    private final SpawnModule spawnModule;
    private final SpawnExtractor spawnExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        FPlayer fPlayer = event.getFPlayer();
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (translationKey == MinecraftTranslationKey.BLOCK_MINECRAFT_SET_SPAWN
                || translationKey == MinecraftTranslationKey.BLOCK_MINECRAFT_BED_SET_SPAWN) {
            event.setCancelled(true);
            spawnModule.send(fPlayer, translationKey);
            return;
        }

        if (!translationKey.startsWith("commands.spawnpoint.success")
                && translationKey != MinecraftTranslationKey.COMMANDS_SETWORLDSPAWN_SUCCESS
                && translationKey != MinecraftTranslationKey.COMMANDS_SETWORLDSPAWN_SUCCESS_NEW) return;

        Optional<Spawn> spawn = spawnExtractor.extract(translationKey, event.getTranslatableComponent());
        if (spawn.isEmpty()) return;

        event.setCancelled(true);
        spawnModule.send(fPlayer, translationKey, spawn.get());
    }

}
