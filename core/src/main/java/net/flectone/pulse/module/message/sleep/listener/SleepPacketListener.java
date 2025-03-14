package net.flectone.pulse.module.message.sleep.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.sleep.SleepModule;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class SleepPacketListener extends AbstractPacketListener {

    private final SleepModule sleepModule;

    @Inject
    public SleepPacketListener(SleepModule sleepModule) {
        this.sleepModule = sleepModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        TranslatableComponent translatableComponent = getTranslatableComponent(event);
        if (translatableComponent == null) return;

        String key = translatableComponent.key();
        if (cancelMessageNotDelivered(event, key)) return;
        if (!key.startsWith("sleep.")) return;
        if (!sleepModule.isEnable()) return;

        String sleepCount = "";
        String allCount = "";

        if (key.startsWith("sleep.players_sleeping") && translatableComponent.args().size() == 2) {
            if ((translatableComponent.args().get(0) instanceof TextComponent sleepComponent)) {
                sleepCount = sleepComponent.content();
            }
            if ((translatableComponent.args().get(1) instanceof TextComponent allComponent)) {
                allCount = allComponent.content();
            }
        }

        event.setCancelled(true);
        sleepModule.send(event.getUser().getUUID(), key, sleepCount, allCount);
    }
}
