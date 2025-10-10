package net.flectone.pulse.module.message.deop.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.deop.DeopModule;
import net.flectone.pulse.module.message.deop.extractor.DeopExtractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DeopPulseListener implements PulseListener {

    private final DeopModule deopModule;
    private final DeopExtractor deopExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (translationKey != MinecraftTranslationKey.COMMANDS_DEOP_SUCCESS) return;

        Optional<FEntity> target = deopExtractor.extract(event.getTranslatableComponent());
        if (target.isEmpty()) return;

        event.setCancelled(true);
        deopModule.send(event.getFPlayer(), translationKey, target.get());
    }

}
