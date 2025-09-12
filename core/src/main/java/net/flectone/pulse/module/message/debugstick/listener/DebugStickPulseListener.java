package net.flectone.pulse.module.message.debugstick.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.debugstick.DebugstickModule;
import net.flectone.pulse.module.message.debugstick.extractor.DebugStickExtractor;
import net.flectone.pulse.module.message.debugstick.model.DebugStick;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class DebugStickPulseListener implements PulseListener {

    private final DebugStickExtractor debugStickExtractor;
    private final DebugstickModule debugstickModule;

    @Inject
    public DebugStickPulseListener(DebugStickExtractor debugStickExtractor,
                                   DebugstickModule debugstickModule) {
        this.debugStickExtractor = debugStickExtractor;
        this.debugstickModule = debugstickModule;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (!translationKey.startsWith("item.minecraft.debug_stick.")) return;

        Optional<DebugStick> optionalDebugStick = debugStickExtractor.extract(translationKey, event.getTranslatableComponent());
        if (optionalDebugStick.isEmpty()) return;

        event.setCancelled(true);
        debugstickModule.send(event.getFPlayer(), translationKey, optionalDebugStick.get());
    }
}