package net.flectone.pulse.module.message.setblock.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.setblock.SetblockModule;
import net.flectone.pulse.module.message.setblock.extractor.SetblockExtractor;
import net.flectone.pulse.module.message.setblock.model.Setblock;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class SetblockPulseListener implements PulseListener {

    private final SetblockModule setblockModule;
    private final SetblockExtractor setblockExtractor;

    @Inject
    public SetblockPulseListener(SetblockModule setblockModule,
                                 SetblockExtractor setblockExtractor) {
        this.setblockModule = setblockModule;
        this.setblockExtractor = setblockExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (translationKey != MinecraftTranslationKey.COMMANDS_SETBLOCK_SUCCESS) return;

        Optional<Setblock> setblock = setblockExtractor.extract(event.getTranslatableComponent());
        if (setblock.isEmpty()) return;

        event.setCancelled(true);
        setblockModule.send(event.getFPlayer(), translationKey, setblock.get());
    }
}
