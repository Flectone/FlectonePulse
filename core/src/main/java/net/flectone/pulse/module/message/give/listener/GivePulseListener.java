package net.flectone.pulse.module.message.give.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.give.GiveModule;
import net.flectone.pulse.module.message.give.extractor.GiveExtractor;
import net.flectone.pulse.module.message.give.model.Give;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class GivePulseListener implements PulseListener {

    private final GiveModule giveModule;
    private final GiveExtractor giveExtractor;

    @Inject
    public GivePulseListener(GiveModule giveModule,
                             GiveExtractor giveExtractor) {
        this.giveModule = giveModule;
        this.giveExtractor = giveExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (!translationKey.startsWith("commands.give.success")) return;

        Optional<Give> optionalGive = giveExtractor.extract(translationKey, event.getTranslatableComponent());
        if (optionalGive.isEmpty()) return;

        event.setCancelled(true);
        giveModule.send(event.getFPlayer(), event.getTranslationKey(), optionalGive.get());
    }

}