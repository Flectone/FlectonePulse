package net.flectone.pulse.module.message.kill.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.module.message.kill.KillModule;
import net.flectone.pulse.module.message.kill.extractor.KillExtractor;
import net.flectone.pulse.module.message.kill.model.Kill;

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
    public void onTranslatableMessageReceiveEvent(TranslatableMessageReceiveEvent event) {
        Optional<Kill> kill = switch (event.getKey()) {
            case COMMANDS_KILL_SUCCESS_MULTIPLE -> killExtractor.extractMultipleKill(event);
            case COMMANDS_KILL_SUCCESS_SINGLE, COMMANDS_KILL_SUCCESS -> killExtractor.extractSingleKill(event);
            default -> Optional.empty();
        };

        if (kill.isEmpty()) return;

        event.setCancelled(true);
        killModule.send(event.getFPlayer(), event.getKey(), kill.get());
    }

}
