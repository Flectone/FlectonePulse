package net.flectone.pulse.module.message.clone.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.clone.CloneModule;
import net.flectone.pulse.module.message.clone.extractor.CloneExtractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class ClonePulseListener implements PulseListener {

    private final CloneModule cloneModule;
    private final CloneExtractor clearExtractor;

    @Inject
    public ClonePulseListener(CloneModule cloneModule,
                              CloneExtractor clearExtractor) {
        this.cloneModule = cloneModule;
        this.clearExtractor = clearExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        if (event.getTranslationKey() != MinecraftTranslationKey.COMMANDS_CLONE_SUCCESS) return;

        Optional<String> optionalAmount = clearExtractor.extract(event.getTranslatableComponent());
        if (optionalAmount.isEmpty()) return;

        event.setCancelled(true);
        cloneModule.send(event.getFPlayer(), optionalAmount.get());
    }

}
