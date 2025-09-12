package net.flectone.pulse.module.message.locate.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.locate.LocateModule;
import net.flectone.pulse.module.message.locate.extractor.LocateExtractor;
import net.flectone.pulse.module.message.locate.model.Locate;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class LocatePulseListener implements PulseListener {

    private final LocateModule locateModule;
    private final LocateExtractor locateExtractor;

    @Inject
    public LocatePulseListener(LocateModule locateModule,
                               LocateExtractor locateExtractor) {
        this.locateModule = locateModule;
        this.locateExtractor = locateExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        switch (translationKey) {
            case COMMANDS_LOCATE_BIOME_SUCCESS, COMMANDS_LOCATE_POI_SUCCESS, COMMANDS_LOCATE_STRUCTURE_SUCCESS -> {
                Optional<Locate> optionalLocate = locateExtractor.extract(event.getTranslatableComponent());
                if (optionalLocate.isEmpty()) return;

                event.setCancelled(true);
                locateModule.send(event.getFPlayer(), translationKey, optionalLocate.get());
            }
        }
    }

}