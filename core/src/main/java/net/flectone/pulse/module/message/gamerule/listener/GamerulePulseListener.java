package net.flectone.pulse.module.message.gamerule.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.gamerule.GameruleModule;
import net.flectone.pulse.module.message.gamerule.extractor.GameruleExtractor;
import net.flectone.pulse.module.message.gamerule.model.Gamerule;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class GamerulePulseListener implements PulseListener {

    private final GameruleModule gameruleModule;
    private final GameruleExtractor gameruleExtractor;

    @Inject
    public GamerulePulseListener(GameruleModule gameruleModule,
                                 GameruleExtractor gameruleExtractor) {
        this.gameruleModule = gameruleModule;
        this.gameruleExtractor = gameruleExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        switch (translationKey) {
            case COMMANDS_GAMERULE_QUERY, COMMANDS_GAMERULE_SET, COMMANDS_GAMERULE_SUCCESS -> {
                Optional<Gamerule> gamerule = gameruleExtractor.extract(translationKey, event.getTranslatableComponent());
                if (gamerule.isEmpty()) return;

                event.setCancelled(true);
                gameruleModule.send(event.getFPlayer(), event.getTranslationKey(), gamerule.get());
            }
        }
    }

}
