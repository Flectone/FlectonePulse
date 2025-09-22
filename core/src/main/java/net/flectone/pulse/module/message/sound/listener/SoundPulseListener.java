package net.flectone.pulse.module.message.sound.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.sound.SoundModule;
import net.flectone.pulse.module.message.sound.extractor.SoundExtractor;
import net.flectone.pulse.module.message.sound.model.Sound;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class SoundPulseListener implements PulseListener {

    private final SoundModule soundModule;
    private final SoundExtractor soundExtractor;

    @Inject
    public SoundPulseListener(SoundModule soundModule,
                              SoundExtractor soundExtractor) {
        this.soundModule = soundModule;
        this.soundExtractor = soundExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        switch (translationKey) {
            case COMMANDS_PLAYSOUND_SUCCESS_MULTIPLE, COMMANDS_PLAYSOUND_SUCCESS_SINGLE,
                 COMMANDS_STOPSOUND_SUCCESS_SOURCE_ANY, COMMANDS_STOPSOUND_SUCCESS_SOURCE_SOUND,
                 COMMANDS_STOPSOUND_SUCCESS_SOURCELESS_ANY, COMMANDS_STOPSOUND_SUCCESS_SOURCELESS_SOUND -> {
                Optional<Sound> sound = soundExtractor.extract(translationKey, event.getTranslatableComponent());
                if (sound.isEmpty()) return;

                event.setCancelled(true);
                soundModule.send(event.getFPlayer(), event.getTranslationKey(), sound.get());
            }
        }
    }

}
