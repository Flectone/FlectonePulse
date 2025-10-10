package net.flectone.pulse.module.message.fillbiome.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.fillbiome.FillbiomeModule;
import net.flectone.pulse.module.message.fillbiome.extractor.FillbiomeExtractor;
import net.flectone.pulse.module.message.fillbiome.model.Fillbiome;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FillbiomePulseListener implements PulseListener {

    private final FillbiomeModule fillbiomeModule;
    private final FillbiomeExtractor fillbiomeExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (!translationKey.startsWith("commands.fillbiome.success")) return;

        Optional<Fillbiome> optionalFillbiome = fillbiomeExtractor.extract(translationKey, event.getTranslatableComponent());
        if (optionalFillbiome.isEmpty()) return;

        event.setCancelled(true);
        fillbiomeModule.send(event.getFPlayer(), translationKey, optionalFillbiome.get());
    }

}