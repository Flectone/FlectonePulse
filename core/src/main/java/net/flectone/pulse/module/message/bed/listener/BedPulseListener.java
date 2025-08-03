package net.flectone.pulse.module.message.bed.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.module.message.bed.BedModule;

@Singleton
public class BedPulseListener implements PulseListener {

    private final BedModule bedModule;

    @Inject
    public BedPulseListener(BedModule bedModule) {
        this.bedModule = bedModule;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(TranslatableMessageReceiveEvent event) {
        if (!event.getKey().startsWith("block.minecraft.bed.") && !event.getKey().startsWith("tile.bed")) return;

        event.setCancelled(true);
        bedModule.send(event.getFPlayer(), event.getKey());
    }

}
