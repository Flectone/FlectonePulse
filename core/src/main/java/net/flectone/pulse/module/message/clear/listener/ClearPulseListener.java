package net.flectone.pulse.module.message.clear.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.clear.ClearModule;
import net.flectone.pulse.module.message.clear.extractor.ClearExtractor;
import net.flectone.pulse.module.message.clear.model.Clear;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ClearPulseListener implements PulseListener {

    private final ClearModule clearModule;
    private final ClearExtractor clearExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (!translationKey.startsWith("commands.clear.success")) return;

        Optional<Clear> clear = clearExtractor.extract(translationKey, event.getTranslatableComponent());
        if (clear.isEmpty()) return;

        event.setCancelled(true);
        clearModule.send(event.getFPlayer(), translationKey, clear.get());
    }

}
