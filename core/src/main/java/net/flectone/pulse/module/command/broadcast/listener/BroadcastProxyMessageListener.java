package net.flectone.pulse.module.command.broadcast.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.broadcast.BroadcastModule;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BroadcastProxyMessageListener implements PulseListener {

    private final BroadcastModule broadcastModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_BROADCAST) return event;
        if (moduleController.isDisabledFor(broadcastModule, event.sender())) return event.withProcessed(true);
        if (!broadcastModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            String message = proxyPayload.readString();

            messageDispatcher.dispatch(broadcastModule, EventMetadata.<Localization.Command.Broadcast>builder()
                    .uuid(event.uuid())
                    .sender(event.sender())
                    .format(Localization.Command.Broadcast::format)
                    .range(Range.get(Range.Type.SERVER))
                    .destination(broadcastModule.config().destination())
                    .message(message)
                    .sound(broadcastModule.soundOrThrow())
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
