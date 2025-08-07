package net.flectone.pulse.module.message.deop.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.deop.DeopModule;
import net.flectone.pulse.module.message.deop.extractor.DeopExtractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class DeopPulseListener implements PulseListener {

    private final DeopModule deopModule;
    private final DeopExtractor deopExtractor;

    @Inject
    public DeopPulseListener(DeopModule deopModule,
                             DeopExtractor deopExtractor) {
        this.deopModule = deopModule;
        this.deopExtractor = deopExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        if (event.getTranslationKey() != MinecraftTranslationKey.COMMANDS_DEOP_SUCCESS) return;

        Optional<String> target = deopExtractor.extract(event);
        if (target.isEmpty()) return;

        event.setCancelled(true);
        deopModule.send(event.getFPlayer(), target.get());
    }

}
