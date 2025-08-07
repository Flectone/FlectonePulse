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
        if (!translationKey.startsWith("block.minecraft.bed.") && !translationKey.startsWith("tile.bed")) return;

        event.setCancelled(true);
        bedModule.send(event.getFPlayer(), translationKey);
    }

}
