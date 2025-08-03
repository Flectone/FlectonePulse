package net.flectone.pulse.module.message.sleep.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.module.message.sleep.SleepModule;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class SleepPulseListener implements PulseListener {

    private final SleepModule sleepModule;

    @Inject
    public SleepPulseListener(SleepModule sleepModule) {
        this.sleepModule = sleepModule;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(TranslatableMessageReceiveEvent event) {
        if (!event.getKey().startsWith("sleep.")) return;

        String sleepCount = "";
        String allCount = "";

        TranslatableComponent translatableComponent = event.getComponent();
        if (event.getKey() == MinecraftTranslationKey.SLEEP_PLAYERS_SLEEPING && translatableComponent.args().size() == 2) {
            if ((translatableComponent.args().get(0) instanceof TextComponent sleepComponent)) {
                sleepCount = sleepComponent.content();
            }
            if ((translatableComponent.args().get(1) instanceof TextComponent allComponent)) {
                allCount = allComponent.content();
            }
        }

        event.setCancelled(true);
        sleepModule.send(event.getFPlayer(), event.getKey(), sleepCount, allCount);
    }

}
