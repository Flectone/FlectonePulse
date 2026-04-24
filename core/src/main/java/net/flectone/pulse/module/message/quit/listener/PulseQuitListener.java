package net.flectone.pulse.module.message.quit.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.lifecycle.DisableEvent;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.service.FPlayerService;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseQuitListener implements PulseListener {

    private final QuitModule quitModule;
    private final FPlayerService fPlayerService;

    @Pulse(priority = Event.Priority.LOW)
    public void onDisableEvent(DisableEvent event) {
        fPlayerService.getPlatformFPlayers().forEach(fPlayer -> quitModule.send(fPlayer, false));
    }

}
