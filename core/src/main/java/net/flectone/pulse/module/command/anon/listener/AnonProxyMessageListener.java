package net.flectone.pulse.module.command.anon.listener;

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
import net.flectone.pulse.module.command.anon.AnonModule;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AnonProxyMessageListener implements PulseListener {

    private final AnonModule anonModule;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_ANON) return event;
        if (moduleController.isDisabledFor(anonModule, event.sender())) return event.withProcessed(true);
        if (!anonModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            String message = proxyPayload.readString();

            messageDispatcher.dispatch(anonModule, EventMetadata.<Localization.Command.Anon>builder()
                    .uuid(event.uuid())
                    .sender(event.sender())
                    .format(Localization.Command.Anon::format)
                    .range(Range.get(Range.Type.SERVER))
                    .destination(anonModule.config().destination())
                    .sound(anonModule.soundOrThrow())
                    .message(message)
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
