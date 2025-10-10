package net.flectone.pulse.module.message.clone.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.clone.CloneModule;
import net.flectone.pulse.module.message.clone.extractor.CloneExtractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ClonePulseListener implements PulseListener {

    private final CloneModule cloneModule;
    private final CloneExtractor clearExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (translationKey != MinecraftTranslationKey.COMMANDS_CLONE_SUCCESS) return;

        Optional<String> optionalAmount = clearExtractor.extract(event.getTranslatableComponent());
        if (optionalAmount.isEmpty()) return;

        event.setCancelled(true);
        cloneModule.send(event.getFPlayer(), translationKey, optionalAmount.get());
    }

}
