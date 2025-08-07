package net.flectone.pulse.module.message.death.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.death.DeathModule;
import net.flectone.pulse.module.message.death.extractor.DeathExtractor;
import net.flectone.pulse.module.message.death.model.Death;
import net.flectone.pulse.module.message.death.model.Item;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class DeathPulseListener implements PulseListener {

    private final DeathModule deathModule;
    private final DeathExtractor deathExtractor;

    @Inject
    public DeathPulseListener(DeathModule deathModule,
                              DeathExtractor deathExtractor) {
        this.deathModule = deathModule;
        this.deathExtractor = deathExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (!translationKey.startsWith("death.")) return;

        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        Death death = deathExtractor.extractDeath(translatableComponent, 0);
        if (death == null) return;

        Death killer = deathExtractor.extractDeath(translatableComponent, 1);
        death.setKiller(killer);

        Item item = deathExtractor.extractItem(translatableComponent);
        death.setItem(item);

        event.setCancelled(true);
        deathModule.send(event.getFPlayer(), death);
    }
}
