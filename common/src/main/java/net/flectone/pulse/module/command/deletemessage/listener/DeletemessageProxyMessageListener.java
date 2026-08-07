package net.flectone.pulse.module.command.deletemessage.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.module.message.format.moderation.delete.DeleteModule;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DeletemessageProxyMessageListener implements PulseListener {

    private final DeleteModule deleteModule;
    
    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_DELETEMESSAGE) return event;

        try (ProxyPayload proxyPayload = event.openPayload()) {
            UUID metadataUUID = UUID.fromString(proxyPayload.readString());
            deleteModule.remove(event.sender(), metadataUUID);
        }

        return event.withProcessed(true);
    }
    
}
