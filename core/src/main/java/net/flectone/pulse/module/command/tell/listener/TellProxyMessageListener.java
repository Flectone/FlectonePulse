package net.flectone.pulse.module.command.tell.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TellProxyMessageListener implements PulseListener {

    private final TellModule tellModule;
    private final ModuleController moduleController;
    private final FPlayerService fPlayerService;
    private final SocialService socialService;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_TELL) return event;
        if (moduleController.isDisabledFor(tellModule, event.sender())) return event.withProcessed(true);
        if (!tellModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            UUID receiverUUID = UUID.fromString(proxyPayload.readString());
            String message = proxyPayload.readString();

            FPlayer fReceiver = fPlayerService.getFPlayer(receiverUUID);
            if (fReceiver.isUnknown()) return event.withProcessed(true);
            if (!socialService.canSeeVanished(fReceiver, event.sender())) return event.withProcessed(true);

            tellModule.send(event.sender(), fReceiver, fReceiver, Localization.Command.Tell::receiver, message, event.uuid());
        }

        return event.withProcessed(true);
    }

}
