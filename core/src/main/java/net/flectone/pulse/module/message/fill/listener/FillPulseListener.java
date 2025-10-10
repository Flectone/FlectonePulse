package net.flectone.pulse.module.message.fill.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.fill.FillModule;
import net.flectone.pulse.module.message.fill.extractor.FillExtractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FillPulseListener implements PulseListener {

    private final FillModule fillModule;
    private final FillExtractor fillExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (translationKey != MinecraftTranslationKey.COMMANDS_FILL_SUCCESS) return;

        Optional<String> blocks = fillExtractor.extract(event.getTranslatableComponent());
        if (blocks.isEmpty()) return;

        event.setCancelled(true);
        fillModule.send(event.getFPlayer(), translationKey, blocks.get());
    }

}
