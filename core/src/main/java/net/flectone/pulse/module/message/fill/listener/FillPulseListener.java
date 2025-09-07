package net.flectone.pulse.module.message.fill.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.fill.FillModule;
import net.flectone.pulse.module.message.fill.extractor.FillExtractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class FillPulseListener implements PulseListener {

    private final FillModule fillModule;
    private final FillExtractor fillExtractor;

    @Inject
    public FillPulseListener(FillModule fillModule,
                             FillExtractor fillExtractor) {
        this.fillModule = fillModule;
        this.fillExtractor = fillExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        if (event.getTranslationKey() != MinecraftTranslationKey.COMMANDS_FILL_SUCCESS) return;

        Optional<String> optionalAmount = fillExtractor.extract(event.getTranslatableComponent());
        if (optionalAmount.isEmpty()) return;

        event.setCancelled(true);
        fillModule.send(event.getFPlayer(), optionalAmount.get());
    }

}
