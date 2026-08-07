package net.flectone.pulse.module.command.clearchat.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.module.command.clearchat.ClearchatModule;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ClearchatProxyMessageListener implements PulseListener {

    private final ClearchatModule clearchatModule;
    private final ModuleController moduleController;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_CLEARCHAT) return event;
        if (!(event.sender() instanceof FPlayer fPlayer)) return event.withProcessed(true);
        if (!moduleController.isEnable(clearchatModule)) return event.withProcessed(true);

        clearchatModule.clearChat(fPlayer, false);

        return event.withProcessed(true);
    }

}
