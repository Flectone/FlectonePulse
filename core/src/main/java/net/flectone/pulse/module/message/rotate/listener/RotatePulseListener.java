package net.flectone.pulse.module.message.rotate.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.rotate.RotateModule;
import net.flectone.pulse.module.message.rotate.extractor.RotateExtractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class RotatePulseListener implements PulseListener {

    private final RotateModule rotateModule;
    private final RotateExtractor rotateExtractor;

    @Inject
    public RotatePulseListener(RotateModule rotateModule,
                               RotateExtractor rotateExtractor) {
        this.rotateModule = rotateModule;
        this.rotateExtractor = rotateExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        if (event.getTranslationKey() != MinecraftTranslationKey.COMMANDS_ROTATE_SUCCESS) return;

        Optional<FEntity> target = rotateExtractor.extract(event);
        if (target.isEmpty()) return;

        event.setCancelled(true);
        rotateModule.send(event.getFPlayer(), target.get());
    }

}