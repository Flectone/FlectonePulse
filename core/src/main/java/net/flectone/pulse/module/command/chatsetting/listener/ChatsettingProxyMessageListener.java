package net.flectone.pulse.module.command.chatsetting.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.util.constant.ModuleName;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ChatsettingProxyMessageListener implements PulseListener {

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_CHATSETTING) return event;

        return event.withProcessed(true);
    }

}
