package net.flectone.pulse.module.message.bed.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.bed.BedModule;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

@Singleton
public class BedPulseListener implements PulseListener {

    private final BedModule bedModule;

    @Inject
    public BedPulseListener(BedModule bedModule) {
        this.bedModule = bedModule;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        switch (translationKey) {
            case BLOCK_MINECRAFT_BED_NO_SLEEP, TILE_BED_NO_SLEEP,
                 BLOCK_MINECRAFT_BED_NOT_SAFE, TILE_BED_NOT_SAFE,
                 BLOCK_MINECRAFT_BED_OBSTRUCTED, BLOCK_MINECRAFT_SPAWN_NOT_VALID,
                 BLOCK_MINECRAFT_BED_NOT_VALID, TILE_BED_NOT_VALID,
                 BLOCK_MINECRAFT_BED_OCCUPIED, TILE_BED_OCCUPIED,
                 BLOCK_MINECRAFT_BED_TOO_FAR_AWAY -> {
                event.setCancelled(true);
                bedModule.send(event.getFPlayer(), translationKey);
            }
        }
    }

}
