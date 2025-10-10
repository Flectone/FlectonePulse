package net.flectone.pulse.module.message.save.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.save.SaveModule;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SavePulseListener implements PulseListener {

    private final SaveModule saveModule;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        switch (translationKey) {
            case COMMANDS_SAVE_DISABLED, COMMANDS_SAVE_ENABLED, COMMANDS_SAVE_SAVING,
                 COMMANDS_SAVE_SUCCESS, COMMANDS_SAVE_START -> {
                event.setCancelled(true);
                saveModule.send(event.getFPlayer(), translationKey);
            }
        }
    }

}