package net.flectone.pulse.module.message.kill.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.kill.KillModule;
import net.flectone.pulse.module.message.kill.extractor.KillExtractor;
import net.flectone.pulse.module.message.kill.model.Kill;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class KillPulseListener implements PulseListener {

    private final KillModule killModule;
    private final KillExtractor killExtractor;

    @Inject
    public KillPulseListener(KillModule killModule,
                             KillExtractor killExtractor) {
        this.killModule = killModule;
        this.killExtractor = killExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        switch (translationKey) {
            case COMMANDS_KILL_SUCCESS_MULTIPLE, COMMANDS_KILL_SUCCESS_SINGLE, COMMANDS_KILL_SUCCESSFUL -> {
                Optional<Kill> kill = killExtractor.extract(translationKey, event.getTranslatableComponent());
                if (kill.isEmpty()) return;

                event.setCancelled(true);
                killModule.send(event.getFPlayer(), translationKey, kill.get());
            }
        }
    }

}
