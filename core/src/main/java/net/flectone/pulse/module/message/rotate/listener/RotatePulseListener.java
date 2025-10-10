package net.flectone.pulse.module.message.rotate.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.rotate.RotateModule;
import net.flectone.pulse.module.message.rotate.extractor.RotateExtractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RotatePulseListener implements PulseListener {

    private final RotateModule rotateModule;
    private final RotateExtractor rotateExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (translationKey != MinecraftTranslationKey.COMMANDS_ROTATE_SUCCESS) return;

        Optional<FEntity> target = rotateExtractor.extract(event.getTranslatableComponent());
        if (target.isEmpty()) return;

        event.setCancelled(true);
        rotateModule.send(event.getFPlayer(), translationKey, target.get());
    }

}